package com.onlyoffice.docspacepipedrive.client.docspace.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Builder
@Data
public class DocspaceRoomInvitationRequest {
    private List<DocspaceRoomInvitation> invitations;
    private boolean notify;
    private String message;
}
