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
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUserSettings;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import com.onlyoffice.docspacepipedrive.web.aop.docspace.DocspaceAction;
import com.onlyoffice.docspacepipedrive.web.aop.docspace.ExecuteDocspaceAction;
import com.onlyoffice.docspacepipedrive.web.dto.room.RoomResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;


@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final RoomMapper roomMapper;
    private final PipedriveClient pipedriveClient;
    private final DocspaceClient docspaceClient;
    private final DocspaceActionManager docspaceActionManager;

    @GetMapping("/{dealId}")
    public ResponseEntity<RoomResponse> findByDealId(
            @AuthenticationPrincipal(expression = "client") Client currentClient,
            @PathVariable Long dealId
    ) {
        return ResponseEntity.ok(
                roomMapper.roomToRoomResponse(roomService.findByClientIdAndDealId(currentClient.getId(), dealId))
        );
    }

    @PostMapping("/{dealId}")
    @Transactional
    @ExecuteDocspaceAction(action = DocspaceAction.INVITE_DEAL_FOLLOWERS_TO_ROOM, execution = Execution.AFTER)
    public ResponseEntity<RoomResponse> create(@AuthenticationPrincipal(expression = "client") Client currentClient,
                                               @PathVariable Long dealId) {
        PipedriveUserSettings pipedriveUserSettings = pipedriveClient.getUserSettings();

        PipedriveDeal pipedriveDeal = pipedriveClient.getDeal(dealId);

        DocspaceRoom docspaceRoom = docspaceClient.createRoom(
                MessageFormat.format(
                        "Pipedrive - {0} - {1}",
                        currentClient.getCompanyName(),
                        pipedriveDeal.getTitle()
                ),
                2
        );

        int visibleToEveryone = PipedriveDeal.VisibleTo.EVERYONE.integer();
        if (pipedriveUserSettings.getAdvancedPermissions()) {
            visibleToEveryone = PipedriveDeal.VisibleTo.EVERYONE_ADVANCED_PERMISSIONS.integer();
        }

        if (pipedriveDeal.getVisibleTo().equals(visibleToEveryone)) {
            docspaceActionManager.inviteSharedGroupToRoom(docspaceRoom.getId());
        }

        Room room = Room.builder()
                .roomId(docspaceRoom.getId())
                .dealId(pipedriveDeal.getId())
                .build();

        Room createdRoom = roomService.create(currentClient.getId(), room);

        return ResponseEntity.ok(roomMapper.roomToRoomResponse(createdRoom));
    }
}
