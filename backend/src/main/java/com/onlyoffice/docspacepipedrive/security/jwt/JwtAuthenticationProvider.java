package com.onlyoffice.docspacepipedrive.security.jwt;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.service.UserService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Map;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Value("${spring.security.oauth2.client.registration.pipedrive.client-secret}")
    private String secret;
    @Value("${spring.security.jwt.client-name-attribute}")
    private String clientNameAttribute;
    @Value("${spring.security.jwt.user-name-attribute}")
    private String userNameAttribute;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(BearerTokenAuthenticationToken.class, authentication, () -> {
            return this.messages.getMessage("JwtAuthenticationProvider.onlySupports", "Only BearerTokenAuthenticationToken is supported");
        });

        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken)authentication;

        Map<String, Object> body;
        try {
            body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(bearer.getToken())
                    .getBody();
        } catch (Exception e) {
            log.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(e.getMessage(), e);
        }

        Long clientId = Long.valueOf((Integer) body.get(clientNameAttribute));
        Long userId = Long.valueOf((Integer) body.get(userNameAttribute));

        User user;
        try {
            user = userService.findByUserIdAndClientId(userId, clientId);
        } catch (UserNotFoundException e) {
            log.debug(MessageFormat.format("Failed to authenticate: {0}", e.getMessage()));
            throw new InvalidBearerTokenException(e.getMessage(), e);
        }

        return createSuccessAuthentication(user, authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(User principal, Authentication authentication) {
        JwtAuthenticationToken result = new JwtAuthenticationToken(principal);
        result.setDetails(authentication.getDetails());

        log.debug("Authenticated user");
        return result;
    }
}
