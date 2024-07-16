package com.onlyoffice.docspacepipedrive.client.docspace.response;

import com.fasterxml.jackson.annotation.JsonValue;


public enum DocspaceAccess {
    NONE,
    READ_WRITE,
    READ,
    RESTRICT,
    VARIES,
    REVIEW,
    COMMENT,
    FILL_FORMS,
    CUSTOM_FILTER,
    ROOM_ADMIN,
    EDITING,
    COLLABORATOR;

    @JsonValue
    public int toValue() {
        return ordinal();
    }
}
