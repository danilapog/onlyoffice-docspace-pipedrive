package com.onlyoffice.docspacepipedrive.exceptions;

import java.text.MessageFormat;
import java.util.UUID;


public class WebhookNotFoundException extends RuntimeException {
    public WebhookNotFoundException(UUID id) {
        super(MessageFormat.format(
                "Webhook with ID ({0}) not found.",
                id.toString()
        ));
    }
}
