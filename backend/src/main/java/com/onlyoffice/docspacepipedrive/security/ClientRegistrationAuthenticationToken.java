package com.onlyoffice.docspacepipedrive.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Collection;

public class ClientRegistrationAuthenticationToken extends AbstractAuthenticationToken {
    private final ClientRegistration principal;

    public ClientRegistrationAuthenticationToken(ClientRegistration principal) {
        super((Collection)null);
        this.principal = principal;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
