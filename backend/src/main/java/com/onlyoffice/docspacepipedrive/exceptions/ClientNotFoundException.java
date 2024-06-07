package com.onlyoffice.docspacepipedrive.exceptions;

import java.text.MessageFormat;


public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(Long clientId) {
        super(MessageFormat.format("Client with ID ({0}) not found.", clientId.toString()));
    }
}
