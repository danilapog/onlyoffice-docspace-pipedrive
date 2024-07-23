package com.onlyoffice.docspacepipedrive.security.provider;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.Webhook;
import com.onlyoffice.docspacepipedrive.exceptions.WebhookNotFoundException;
import com.onlyoffice.docspacepipedrive.security.token.UserAuthenticationToken;
import com.onlyoffice.docspacepipedrive.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookAuthenticationProvider implements AuthenticationProvider {
    private final WebhookService webhookService;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication, () -> {
            return this.messages.getMessage("WebhookAuthenticationProvider.onlySupports", "Only UsernamePasswordAuthenticationToken is supported");
        });

        Webhook webhook;
        try {
            webhook = webhookService.findById(UUID.fromString((String) authentication.getPrincipal()));
        } catch (WebhookNotFoundException e) {
            throw new BadCredentialsException(e.getMessage());
        }

        if (!webhook.getPassword().equals(authentication.getCredentials())) {
            throw new BadCredentialsException(
                    this.messages.getMessage("WebhookAuthenticationProvider.badCredentials", "Bad credentials")
            );
        }

        return createSuccessAuthentication(webhook.getUser(), authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(User principal, Authentication authentication) {
        UserAuthenticationToken result = new UserAuthenticationToken(principal);
        result.setDetails(authentication.getDetails());

        log.debug("Authenticated user");
        return result;
    }
}
