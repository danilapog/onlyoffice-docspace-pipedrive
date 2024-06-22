package com.onlyoffice.docspacepipedrive.exceptions;

import org.springframework.security.oauth2.core.OAuth2AuthorizationException;

public class PipedriveOAuth2AuthorizationException extends OAuth2AuthorizationException {
    public PipedriveOAuth2AuthorizationException(OAuth2AuthorizationException e) {
        super(e.getError());
    }
}
