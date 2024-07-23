package com.onlyoffice.docspacepipedrive.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationAuthenticationProvider implements AuthenticationProvider {
    private final ClientRegistrationRepository clientRegistrationRepository;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication, () -> {
            return this.messages.getMessage("OAuth2ClientCredentialsAuthenticationProvider.onlySupports", "Only UsernamePasswordAuthenticationToken is supported");
        });

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("pipedrive");

        if (clientRegistration == null) {
            throw new BadCredentialsException(
                    this.messages.getMessage("OAuth2ClientCredentialsAuthenticationProvider.badCredentials", "Bad credentials")
            );
        }

        if (!clientRegistration.getClientId().equals(authentication.getPrincipal())
                || !clientRegistration.getClientSecret().equals(authentication.getCredentials())) {
            throw new BadCredentialsException(
                    this.messages.getMessage("OAuth2ClientCredentialsAuthenticationProvider.badCredentials", "Bad credentials")
            );
        }

        return createSuccessAuthentication(clientRegistration, authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(ClientRegistration clientRegistration, Authentication authentication) {
        ClientRegistrationAuthenticationToken result = new ClientRegistrationAuthenticationToken(clientRegistration);
        result.setDetails(authentication.getDetails());

        log.debug("Authenticated clientRegistration");
        return result;
    }
}
