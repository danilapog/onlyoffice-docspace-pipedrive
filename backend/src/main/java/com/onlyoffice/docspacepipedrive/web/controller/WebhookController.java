/**
 *
 * (c) Copyright Ascensio System SIA 2025
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

import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveDeal;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUserSettings;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.events.deal.AddFollowersToPipedriveDealEvent;
import com.onlyoffice.docspacepipedrive.events.deal.AddVisibleEveryoneForPipedriveDealEvent;
import com.onlyoffice.docspacepipedrive.events.deal.RemoveFollowersFromPipedriveDealEvent;
import com.onlyoffice.docspacepipedrive.events.deal.RemoveVisibleEveryoneForPipedriveDealEvent;
import com.onlyoffice.docspacepipedrive.events.user.UserOwnerWebhooksIsLostEvent;
import com.onlyoffice.docspacepipedrive.exceptions.RoomNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.dto.webhook.WebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final PipedriveClient pipedriveClient;
    private final RoomService roomService;
    private final UserService userService;
    private final PipedriveActionManager pipedriveActionManager;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/deal")
    public void updatedDeal(@AuthenticationPrincipal(expression = "client") Client currentClient,
                            @RequestBody WebhookRequest<PipedriveDeal> request) {
        PipedriveDeal currentDeal = request.getCurrent();
        PipedriveDeal previousDeal = request.getPrevious();

        try {
            roomService.findByClientIdAndDealId(currentClient.getId(), currentDeal.getId());
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
                eventPublisher.publishEvent(new AddVisibleEveryoneForPipedriveDealEvent(this, currentDeal));
            }

            if (previousDeal.getVisibleTo().equals(visibleToEveryone)) {
                eventPublisher.publishEvent(new RemoveVisibleEveryoneForPipedriveDealEvent(this, currentDeal));
            }
        }

        // Hook on change deal followers count
        if (!currentDeal.getFollowersCount().equals(previousDeal.getFollowersCount())) {
            // If added follower
            if (currentDeal.getFollowersCount() > previousDeal.getFollowersCount()) {
                eventPublisher.publishEvent(new AddFollowersToPipedriveDealEvent(this, currentDeal));
            }

            // If removed follower
            if (currentDeal.getFollowersCount() < previousDeal.getFollowersCount()) {
                eventPublisher.publishEvent(new RemoveFollowersFromPipedriveDealEvent(this, currentDeal));
            }
        }
    }

    @PostMapping("/user")
    public void updatedUser(@AuthenticationPrincipal User currentUser,
                            @AuthenticationPrincipal(expression = "client") Client currentClient,
                            @RequestBody WebhookRequest<List<PipedriveUser>> request) {
        List<PipedriveUser> currentUsers = request.getCurrent();
        List<PipedriveUser> previousUsers = request.getPrevious();

        boolean isOwnerWebhookIsNotSalesAdmin = currentUsers.stream()
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

        if (isOwnerWebhookIsNotSalesAdmin) {
            eventPublisher.publishEvent(new UserOwnerWebhooksIsLostEvent(this, currentUser));
        }
    }
}
