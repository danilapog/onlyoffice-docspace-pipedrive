package com.onlyoffice.docspacepipedrive.exceptions;

import org.springframework.security.access.AccessDeniedException;

import java.text.MessageFormat;


public class PipedriveAccessDeniedException extends AccessDeniedException {
    public PipedriveAccessDeniedException(Long userId) {
        super(MessageFormat.format("Pipedrive user with ID ({0}) is not permitted to this operation.", userId));
    }
}
