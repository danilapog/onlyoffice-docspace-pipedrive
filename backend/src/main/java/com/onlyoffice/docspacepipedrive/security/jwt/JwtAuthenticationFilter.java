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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;


@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Value("${spring.security.oauth2.client.registration.pipedrive.client-secret}")
    private String jwtSecretKey;
    @Value("${spring.security.jwt.header}")
    private String jwtHeader;
    @Value("${spring.security.jwt.prefix}")
    private String jwtPrefix;
    @Value("${spring.security.jwt.user-name-attribute}")
    private String jwtUserNameAttribute;
    @Value("${spring.security.jwt.client-name-attribute}")
    private String jwtClientNameAttribute;

    private final UserService userService;
    private final JwtManager jwtManager;
    private final AuthenticationFailureHandler failureHandler;


    public JwtAuthenticationFilter(final UserService userService, final JwtManager jwtManager,
                                   final AuthenticationEntryPoint authenticationEntryPoint) {
        this.userService = userService;
        this.jwtManager = jwtManager;

        this.failureHandler = new AuthenticationEntryPointFailureHandler(authenticationEntryPoint);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwtToken = extractTokenFromRequest(request);

        if (jwtToken == null) {
            filterChain.doFilter(request, response);
        } else {
            try {
                JwtAuthenticationToken authentication = attemptAuthentication(jwtToken);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                successfulAuthentication(request, response, filterChain, authentication);
            } catch (AuthenticationException e) {
                unsuccessfulAuthentication(request, response, e);
            }
        }
    }

    public JwtAuthenticationToken attemptAuthentication(String jwtToken)
            throws AuthenticationException {
        Map<String, Object> body;

        try {
            body = jwtManager.getBody(jwtSecretKey, jwtToken);
        } catch (Exception e) {
            throw new JwtAuthenticationException(e.getMessage(), e);
        }

        Long userId = Long.valueOf((Integer) body.get(jwtUserNameAttribute));
        Long clientId = Long.valueOf((Integer) body.get(jwtClientNameAttribute));

        if (userId == null || clientId == null) {
            throw new JwtAuthenticationException("Authorization request is not valid");
        }

        User user;
        try {
            user = userService.findByUserIdAndClientId(userId, clientId);
        } catch (UserNotFoundException e) {
            throw new JwtAuthenticationException(e.getMessage(), e);
        }

        return new JwtAuthenticationToken(user);
    }

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

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.logger.trace("Failed to process authentication request", failed);
        this.logger.trace("Handling authentication failure");
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader(jwtHeader);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(jwtPrefix)) {
            return headerAuth.substring(jwtPrefix.length()).trim();
        }

        return null;
    }
}
