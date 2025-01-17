/**
 *
 * (c) Copyright Ascensio System SIA 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.onlyoffice.docspacepipedrive.security.provider;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.security.token.UserAuthenticationToken;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
    private final JwtDecoder jwtDecoder;
    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Value("${spring.security.jwt.client-name-attribute}")
    private String clientNameAttribute;
    @Value("${spring.security.jwt.user-name-attribute}")
    private String userNameAttribute;


    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(BearerTokenAuthenticationToken.class, authentication, () -> {
            return this.messages.getMessage(
                    "JwtAuthenticationProvider.onlySupports",
                    "Only BearerTokenAuthenticationToken is supported"
            );
        });

        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;

        Map<String, Object> body;
        try {
            body = jwtDecoder.decode(bearer.getToken()).getClaims();
        } catch (Exception e) {
            log.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(e.getMessage(), e);
        }

        Long clientId = (Long) body.get(clientNameAttribute);
        Long userId = (Long) body.get(userNameAttribute);

        User user;
        try {
            user = userService.findByClientIdAndUserId(clientId, userId);
        } catch (UserNotFoundException e) {
            log.debug(MessageFormat.format("Failed to authenticate: {0}", e.getMessage()));
            throw new InvalidBearerTokenException(e.getMessage(), e);
        }

        return createSuccessAuthentication(user, authentication);
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createSuccessAuthentication(final User principal, final Authentication authentication) {
        UserAuthenticationToken result = new UserAuthenticationToken(principal);
        result.setDetails(authentication.getDetails());

        log.debug("Authenticated user");
        return result;
    }
}
