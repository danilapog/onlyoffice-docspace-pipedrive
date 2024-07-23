package com.onlyoffice.docspacepipedrive.security.token;

import com.onlyoffice.docspacepipedrive.entity.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collection;


public class UserAuthenticationToken extends AbstractAuthenticationToken {
    private final User principal;

    public UserAuthenticationToken(User principal) {
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
