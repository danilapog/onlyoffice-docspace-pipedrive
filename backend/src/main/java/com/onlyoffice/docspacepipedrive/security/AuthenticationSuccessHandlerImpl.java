package com.onlyoffice.docspacepipedrive.security;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;


@Component
@AllArgsConstructor
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken auth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(
                auth2AuthenticationToken.getAuthorizedClientRegistrationId()
        );

        Long userId = null;

        if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            userId = Long.valueOf(defaultOAuth2User.getName());
        }

        User user = userService.findById(userId);

        URI redirectUri = UriComponentsBuilder.fromUriString(user.getClient().getUrl())
                .path("settings/marketplace/app/{clientId}/app-setting")
                .build(clientRegistration.getClientId());

        response.sendRedirect(redirectUri.toString());
    }
}
