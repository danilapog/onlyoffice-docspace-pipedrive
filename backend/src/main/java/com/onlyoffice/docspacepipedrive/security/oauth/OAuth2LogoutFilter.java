package com.onlyoffice.docspacepipedrive.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.docspacepipedrive.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;


@Slf4j
@Component
public class OAuth2LogoutFilter extends GenericFilterBean {
    @Setter
    private RequestMatcher logoutRequestMatcher;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final UserService userService;
    private AuthenticationFailureHandler failureHandler;

    public OAuth2LogoutFilter(final ClientRegistrationRepository clientRegistrationRepository,
                              final UserService userService, final AuthenticationEntryPoint authenticationEntryPoint) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.userService = userService;
        this.failureHandler = new AuthenticationEntryPointFailureHandler(authenticationEntryPoint);

        logoutRequestMatcher = new AntPathRequestMatcher("/login/oauth2/code/{registrationId}", "DELETE");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (!logoutRequestMatcher.matches((request))) {
            filterChain.doFilter(request, response);
        } else {
            try {
                authenticate(request, response);

                Map<String, Object> body = extractBodyFromRequest(request);

                Long userId = Long.valueOf((Integer) body.get("user_id"));
                Long clientId = Long.valueOf((Integer) body.get("company_id"));

                userService.deleteByUserIdAndClientId(userId, clientId);
            } catch (AuthenticationException e) {
                unsuccessfulAuthentication(request, response, e);
            }
        }
    }

    public void authenticate(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String registrationId = resolveRegistrationId(request);
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        if (clientRegistration == null) {
            OAuth2Error oauth2Error = new OAuth2Error("client_registration_not_found", "Client Registration not found with Id: " + registrationId, (String)null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        String token = extractTokenFromRequest(request);
        if (token == null) {
            OAuth2Error oauth2Error = new OAuth2Error("authorization_token_not_found", "Authorization token not found", (String)null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }

        if (!token.equals(HttpHeaders.encodeBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret(),
                            (Charset) null))) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_authorization_token_not_found", "Authorization token in invalid", (String)null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.logger.trace("Failed to process authentication request", failed);
        this.logger.trace("Handling authentication failure");
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Basic ")) {
            return headerAuth.substring("Basic ".length());
        }

        return null;
    }

    private Map<String, Object> extractBodyFromRequest(HttpServletRequest request) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(request.getInputStream(), Map.class);
    }

    private String resolveRegistrationId(HttpServletRequest request) {
        return this.logoutRequestMatcher.matches(request)
                ? (String)this.logoutRequestMatcher.matcher(request).getVariables().get("registrationId")
                : null;
    }
}
