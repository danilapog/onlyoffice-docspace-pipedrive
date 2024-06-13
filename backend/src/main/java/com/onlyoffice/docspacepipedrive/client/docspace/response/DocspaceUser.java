package com.onlyoffice.docspacepipedrive.client.docspace.response;

import lombok.Data;

import java.util.UUID;


@Data
public class DocspaceUser {
    private UUID id;
    private String email;
    private Boolean isAdmin;
}
