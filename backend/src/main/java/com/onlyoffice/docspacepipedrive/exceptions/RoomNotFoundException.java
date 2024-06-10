package com.onlyoffice.docspacepipedrive.exceptions;

import java.text.MessageFormat;


public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long dealId) {
        super(MessageFormat.format("Room for deal with ID ({0}) not found.", dealId.toString()));
    }
}
