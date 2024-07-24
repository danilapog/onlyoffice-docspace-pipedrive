package com.onlyoffice.docspacepipedrive.exceptions;

import java.text.MessageFormat;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super(MessageFormat.format(
                "User with ID ({0}) not found.",
                id.toString()
        ));
    }

    public UserNotFoundException(Long userId, Long clientId) {
        super(MessageFormat.format(
                "User with USER_ID ({0}) and CLIENT_ID ({1}) not found.",
                userId.toString(),
                clientId.toString()
        ));
    }
}
