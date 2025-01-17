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

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoom;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveDeal;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveDealFollower;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.events.deal.AddRoomToPipedriveDealEvent;
import com.onlyoffice.docspacepipedrive.exceptions.RoomNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.web.dto.room.RoomResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
@Slf4j
public class RoomController {
    private static final String INTEGRATION_TAG_NAME = "Pipedrive Integration";

    private final RoomService roomService;
    private final RoomMapper roomMapper;
    private final PipedriveClient pipedriveClient;
    private final DocspaceClient docspaceClient;
    private final DocspaceActionManager docspaceActionManager;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/{dealId}")
    public ResponseEntity<RoomResponse> findByDealId(
            @AuthenticationPrincipal(expression = "client") Client currentClient,
            @PathVariable Long dealId
    ) {
        pipedriveClient.getDeal(dealId);

        return ResponseEntity.ok(
                roomMapper.roomToRoomResponse(roomService.findByClientIdAndDealId(currentClient.getId(), dealId))
        );
    }

    @PostMapping("/{dealId}")
    public ResponseEntity<RoomResponse> create(@AuthenticationPrincipal(expression = "client") Client currentClient,
                                               @PathVariable Long dealId) {
        PipedriveDeal pipedriveDeal = pipedriveClient.getDeal(dealId);

        DocspaceRoom docspaceRoom = docspaceClient.createRoom(
                MessageFormat.format(
                        "{0} - Pipedrive ({1})",
                        pipedriveDeal.getTitle(),
                        currentClient.getCompanyName()
                ),
                2,
                Collections.singletonList(INTEGRATION_TAG_NAME)
        );

        Room room = Room.builder()
                .roomId(docspaceRoom.getId())
                .dealId(pipedriveDeal.getId())
                .build();

        Room createdRoom;
        try {
            createdRoom = roomService.update(currentClient.getId(), room);
        } catch (RoomNotFoundException e) {
            createdRoom = roomService.create(currentClient.getId(), room);
        }

        try {
            eventPublisher.publishEvent(
                    new AddRoomToPipedriveDealEvent(this, pipedriveDeal, createdRoom.getRoomId())
            );
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return ResponseEntity.ok(roomMapper.roomToRoomResponse(createdRoom));
    }

    @PostMapping("/{dealId}/request-access")
    public ResponseEntity<Void> requestAccess(@AuthenticationPrincipal User currentUser,
                                     @AuthenticationPrincipal(expression = "client") Client currentClient,
                                     @PathVariable Long dealId) {
        Room room = roomService.findByClientIdAndDealId(currentClient.getId(), dealId);

        List<PipedriveDealFollower> dealFollowers = pipedriveClient.getDealFollowers(dealId);

        boolean currentUserIsDealFollower = dealFollowers.stream()
                .filter(dealFollower -> dealFollower.getUserId().equals(currentUser.getUserId()))
                .findFirst()
                .isPresent();

        SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
            public Void doWork() {
                if (currentUserIsDealFollower) {
                    docspaceActionManager.inviteListDocspaceAccountsToRoom(
                            room.getRoomId(),
                            Collections.singletonList(currentUser.getDocspaceAccount())
                    );
                } else {
                    docspaceActionManager.inviteSharedGroupToRoom(room.getRoomId());
                }
                return null;
            }
        }, currentClient.getSystemUser());

        return ResponseEntity.ok().build();
    }
}
