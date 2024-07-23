package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceRoom;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveDeal;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import com.onlyoffice.docspacepipedrive.web.aop.docspace.DocspaceAction;
import com.onlyoffice.docspacepipedrive.web.aop.docspace.ExecuteDocspaceAction;
import com.onlyoffice.docspacepipedrive.web.dto.room.RoomResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final RoomMapper roomMapper;
    private final PipedriveClient pipedriveClient;
    private final DocspaceClient docspaceClient;

    @GetMapping("/{dealId}")
    public ResponseEntity<RoomResponse> findByDealId(@PathVariable Long dealId) {
        return ResponseEntity.ok(
                roomMapper.roomToRoomResponse(roomService.findByDealId(dealId))
        );
    }

    @PostMapping("/{dealId}")
    @Transactional
    @ExecuteDocspaceAction(action = DocspaceAction.INVITE_DEAL_FOLLOWERS_TO_ROOM, execution = Execution.AFTER)
    public ResponseEntity<RoomResponse> create(@PathVariable Long dealId) {
        User user = SecurityUtils.getCurrentUser();

        PipedriveDeal pipedriveDeal = pipedriveClient.getDeal(dealId);

        DocspaceRoom docspaceRoom = docspaceClient.createRoom(pipedriveDeal.getTitle(), 2);

        Room room = Room.builder()
                .roomId(docspaceRoom.getId())
                .dealId(pipedriveDeal.getId())
                .build();

        Room createdRoom = roomService.create(user.getClient().getId(), room);

        return ResponseEntity.ok(roomMapper.roomToRoomResponse(createdRoom));
    }
}
