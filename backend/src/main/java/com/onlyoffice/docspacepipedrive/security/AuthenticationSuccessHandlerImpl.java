package com.onlyoffice.docspacepipedrive.security;

import com.onlyoffice.docspacepipedrive.entity.Client;
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
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;


@Component
@AllArgsConstructor
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final ClientService clientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken auth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(
                auth2AuthenticationToken.getAuthorizedClientRegistrationId()
        );

        Long clientId = null;

        if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal) {
            OAuth2AuthenticatedPrincipal oAuth2AuthenticatedPrincipal = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
            clientId = ((Number) oAuth2AuthenticatedPrincipal.getAttribute("company_id")).longValue(); //ToDo name from properties
        }

        Client client = clientService.findById(clientId);

        URI redirectUri = UriComponentsBuilder.fromUriString(client.getUrl())
                .path("/settings/marketplace/app/{clientId}/app-setting")
                .build(clientRegistration.getClientId());

        response.sendRedirect(redirectUri.toString());
    }
}
