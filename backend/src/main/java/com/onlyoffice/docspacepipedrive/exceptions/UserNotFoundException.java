package com.onlyoffice.docspacepipedrive.exceptions;

import java.text.MessageFormat;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super(MessageFormat.format("User with ID ({0}) not found.", userId.toString()));
    }
}
