package com.onlyoffice.docspacepipedrive.security.jwt;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.JwtAuthenticationException;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.security.jwt.manager.JwtManager;
import com.onlyoffice.docspacepipedrive.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class JwtLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_FILTER_PROCESSES_URL = "/**";
    private static final String DEFAULT_USER_NAME_ATTRIBUTE = "userId";
    private static final String DEFAULT_AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String DEFAULT_AUTHORIZATION_PREFIX = "Bearer ";


    @Getter(AccessLevel.PROTECTED)
    private final UserService userService;
    private final JwtManager jwtManager;


    @Getter(AccessLevel.PROTECTED)
    @Setter
    private String userNameAttribute;
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private String authorizationHeader;
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private String authorizationPrefix;
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private String secretKey;
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private String oauthUserNameAttribute;


    public JwtLoginAuthenticationFilter(final AuthenticationManager authenticationManager,
                                                final UserService userService,
                                                final JwtManager jwtManager) {
        super(DEFAULT_FILTER_PROCESSES_URL);

        this.userService = userService;
        this.jwtManager = jwtManager;

        this.setAuthenticationManager(authenticationManager);
        this.setUserNameAttribute(DEFAULT_USER_NAME_ATTRIBUTE);
        this.setAuthorizationHeader(DEFAULT_AUTHORIZATION_HEADER);
        this.setAuthorizationPrefix(DEFAULT_AUTHORIZATION_PREFIX);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String jwtToken = extractTokenFromRequest(request);

        if (jwtToken == null) {
            throw new JwtAuthenticationException("Authorization request not found!");
        }

        Map<String, Object> body;

        try {
            body = jwtManager.getBody(getSecretKey(), jwtToken);
        } catch (Exception e) {
            throw new JwtAuthenticationException(e.getMessage(), e);
        }

        Long userId = Long.valueOf((Integer) body.get(userNameAttribute));

        if (userId == null) {
            throw new JwtAuthenticationException("Authorization request is not valid.");
        }

        User user;

        try {
            user = userService.findById(userId);
        } catch (UserNotFoundException e) {
            throw new JwtAuthenticationException(e.getMessage(), e);
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(oauthUserNameAttribute, user.getId());

        var oAuth2User = new DefaultOAuth2User(null, attributes, oauthUserNameAttribute);

        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                oAuth2User,
                null,
                "pipedrive"
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);

        SecurityContextHolder.setContext(context);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authResult));
        }

        chain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader(getAuthorizationHeader());

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(getAuthorizationPrefix())) {
            return headerAuth.substring(getAuthorizationPrefix().length());
        }

        return null;
    }
}
