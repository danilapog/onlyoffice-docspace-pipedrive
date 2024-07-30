/**
 *
 * (c) Copyright Ascensio System SIA 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.onlyoffice.docspacepipedrive.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveDeal;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveDealFollowerEvent;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUserSettings;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.RoomNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.dto.webhook.WebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final PipedriveClient pipedriveClient;
    private final RoomService roomService;
    private final UserService userService;
    private final ClientService clientService;
    private final DocspaceActionManager docspaceActionManager;
    private final PipedriveActionManager pipedriveActionManager;

    @PostMapping("/deal")
    public void updatedDeal(@RequestBody WebhookRequest<PipedriveDeal> request) {
        PipedriveDeal currentDeal = request.getCurrent();
        PipedriveDeal previousDeal = request.getPrevious();

        User currentUser = SecurityUtils.getCurrentUser();
        if (!currentUser.isSystemUser()) {
            pipedriveActionManager.removeWebhooks();
            throw new PipedriveAccessDeniedException(currentUser.getUserId());
        }

        Room room;
        try {
            room = roomService.findByDealId(currentDeal.getId());
        } catch (RoomNotFoundException e) {
            // Ignore it if there is no DocSpace room for the Pipedrive deal
            return;
        }

        // Hook on change deal visible
        if (!currentDeal.getVisibleTo().equals(previousDeal.getVisibleTo())) {
            PipedriveUserSettings pipedriveUserSettings = pipedriveClient.getUserSettings();

            int visibleToEveryone = PipedriveDeal.VisibleTo.EVERYONE.integer();

            if (pipedriveUserSettings.getAdvancedPermissions()) {
                visibleToEveryone = PipedriveDeal.VisibleTo.EVERYONE_ADVANCED_PERMISSIONS.integer();
            }

            if (currentDeal.getVisibleTo().equals(visibleToEveryone)) {
                docspaceActionManager.inviteSharedGroupToRoom(room.getRoomId());
            }

            if (previousDeal.getVisibleTo().equals(visibleToEveryone)) {
                docspaceActionManager.removeSharedGroupFromRoom(room.getRoomId());
            }
        }

        // Hook on change deal followers count
        if (!currentDeal.getFollowersCount().equals(previousDeal.getFollowersCount())) {
            // If added follower
            if (currentDeal.getFollowersCount() > previousDeal.getFollowersCount()) {
                handleEventAddDealFollowers(currentDeal, room);
            }

            // If removed follower
            if (currentDeal.getFollowersCount() < previousDeal.getFollowersCount()) {
                handleEventRemoveDealFollowers(currentDeal, room);
            }
        }
    }

    @PostMapping("/user")
    public void updatedUser(@RequestBody WebhookRequest<List<PipedriveUser>> request) throws JsonProcessingException {
        List<PipedriveUser> currentUsers = request.getCurrent();
        List<PipedriveUser> previousUsers = request.getPrevious();

        User currentUser = SecurityUtils.getCurrentUser();
        if (!currentUser.isSystemUser()) {
            pipedriveActionManager.removeWebhooks();
            throw new PipedriveAccessDeniedException(currentUser.getUserId());
        }

        boolean unsetSystemUser = currentUsers.stream()
                .filter(pipedriveUser -> pipedriveUser.getId().equals(currentUser.getUserId()))
                .filter(pipedriveUser -> !pipedriveUser.isSalesAdmin())
                .filter(pipedriveUser -> {
                    return previousUsers.stream()
                            .filter(previousUser -> previousUser.getId().equals(pipedriveUser.getId()))
                            .filter(previousUser -> previousUser.getAccess().stream()
                                        .filter(access -> access.getAdmin())
                                        .findFirst()
                                        .orElse(null) != null
                            )
                            .findFirst()
                            .orElse(null) != null;
                })
                .findFirst()
                .orElse(null) != null;

        if (unsetSystemUser) {
            clientService.unsetSystemUser(currentUser.getClient().getId());
            pipedriveActionManager.removeWebhooks();
        }
    }

    private void handleEventAddDealFollowers(PipedriveDeal deal, Room room) {
        User currentUser = SecurityUtils.getCurrentUser();

        List<PipedriveDealFollowerEvent> dealFollowerEvents = pipedriveClient.getDealFollowersFlow(deal.getId());

        List<Long> userIdsAddedFollowers = findUserIdsInDealFollowersEvents(
                dealFollowerEvents,
                "added",
                deal.getUpdateTime()
        );

        List<User> addedFollowers = new ArrayList<>();
        for (Long userId : userIdsAddedFollowers) {
            try {
                addedFollowers.add(userService.findByUserIdAndClientId(userId, currentUser.getClient().getId()));
            } catch (UserNotFoundException e) { }
        }

        List<DocspaceAccount> docspaceAccounts = addedFollowers.stream()
                .filter(user -> user.getDocspaceAccount() != null)
                .map(user -> user.getDocspaceAccount())
                .toList();

        docspaceActionManager.inviteListDocspaceAccountsToRoom(room.getRoomId(), docspaceAccounts);
    }

    private void handleEventRemoveDealFollowers(PipedriveDeal deal, Room room) {
        User currentUser = SecurityUtils.getCurrentUser();

        List<PipedriveDealFollowerEvent> dealFollowerEvents = pipedriveClient.getDealFollowersFlow(deal.getId());

        List<Long> userIdsRemovedFollowers = findUserIdsInDealFollowersEvents(
                dealFollowerEvents,
                "removed",
                deal.getUpdateTime()
        );

        List<User> removedFollowers = new ArrayList<>();
        for (Long userId : userIdsRemovedFollowers) {
            try {
                removedFollowers.add(userService.findByUserIdAndClientId(userId, currentUser.getClient().getId()));
            } catch (UserNotFoundException e) { }
        }

        List<DocspaceAccount> docspaceAccounts = removedFollowers.stream()
                .filter(user -> user.getDocspaceAccount() != null)
                .map(user -> user.getDocspaceAccount())
                .toList();

        docspaceActionManager.removeListDocspaceAccountsFromRoom(room.getRoomId(), docspaceAccounts);
    }

    private List<Long> findUserIdsInDealFollowersEvents(List<PipedriveDealFollowerEvent> dealFollowerEvents,
                                                        String action, String time) {
        return dealFollowerEvents.stream()
                .filter(followerEvent -> {
                    return followerEvent.getData().getAction().equals(action)
                            && followerEvent.getData().getLogTime().equals(time);
                })
                .map(follower -> follower.getData().getFollowerUserId())
                .toList();
    }
}
