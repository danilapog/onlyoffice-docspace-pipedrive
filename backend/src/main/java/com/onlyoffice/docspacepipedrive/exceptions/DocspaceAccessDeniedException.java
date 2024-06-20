package com.onlyoffice.docspacepipedrive.exceptions;

import org.springframework.security.access.AccessDeniedException;

import java.text.MessageFormat;


public class DocspaceAccessDeniedException extends AccessDeniedException {
    public DocspaceAccessDeniedException(String userName) {
        super(MessageFormat.format("DocSpace user ({0}) is not permitted to this operation.", userName));
    }
}
