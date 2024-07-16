package com.onlyoffice.docspacepipedrive.client.docspace.request;

import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceAccess;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class DocspaceRoomInvitation {
    private UUID id;
    private DocspaceAccess access;
}
