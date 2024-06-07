package com.onlyoffice.docspacepipedrive.exceptions;

import java.text.MessageFormat;


public class SettingsNotFoundException extends RuntimeException {
    public SettingsNotFoundException(Long clientId) {
        super(MessageFormat.format("Settings for client with ID ({0}) not found.", clientId.toString()));
    }
}
