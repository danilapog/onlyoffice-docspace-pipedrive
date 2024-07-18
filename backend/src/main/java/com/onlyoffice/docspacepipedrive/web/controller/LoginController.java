package com.onlyoffice.docspacepipedrive.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {
    private final ClientRegistrationRepository clientRegistrationRepository;

    //ToDo: redirect to page in marketplace if user cancel installation
    @GetMapping("/oauth2/code/pipedrive")
    public RedirectView loginByOAuth2CodePipedrive() {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("pipedrive");

        URI redirectUrl = UriComponentsBuilder.fromUriString("https://app.pipedrive.com")
                .path("/settings/marketplace/app/{clientId}/app-settings")
                .build(clientRegistration.getClientId());

        return new RedirectView(redirectUrl.toString());
    }
}
