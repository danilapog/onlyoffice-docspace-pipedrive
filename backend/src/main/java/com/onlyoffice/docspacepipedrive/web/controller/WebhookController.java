package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveDeal;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveDealFollowerEvent;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.RoomNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.dto.webhook.deal.WebhookDealRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    private final DocspaceActionManager docspaceActionManager;

    @PostMapping("/deal")
    public void updatedDeal(@RequestBody WebhookDealRequest request) {
        PipedriveDeal currentDeal = request.getCurrent();
        PipedriveDeal previousDeal = request.getPrevious();

        Room room;
        try {
            room = roomService.findByDealId(currentDeal.getId());
        } catch (RoomNotFoundException e) {
            // Ignore it if there is no DocSpace room for the Pipedrive deal
            return;
        }

        // Hook on change deal visible
        if (!currentDeal.getVisibleTo().equals(previousDeal.getVisibleTo())) {
            // If deal visible for all users
            if (currentDeal.getVisibleTo().equals(3)) {
                docspaceActionManager.inviteSharedGroupToRoom(room.getRoomId());
            } else if (currentDeal.getVisibleTo().equals(1)) { // If deal visible for only followers
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
            } catch (UserNotFoundException e) {}
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
            } catch (UserNotFoundException e) {}
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
