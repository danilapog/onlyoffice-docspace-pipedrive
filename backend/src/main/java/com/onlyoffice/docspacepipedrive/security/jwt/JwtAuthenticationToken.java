package com.onlyoffice.docspacepipedrive.security.jwt;

import com.onlyoffice.docspacepipedrive.entity.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collection;


public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final User principal;

    public JwtAuthenticationToken(User principal) {
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
