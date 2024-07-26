package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.dto.login.UninstallRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {
    @Value("${pipedrive.base-url}")
    private String baseUrl;

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final UserService userService;
    private final ClientService clientService;

    //ToDo: redirect to page in marketplace if user cancel installation
    @GetMapping("/oauth2/code/pipedrive")
    public RedirectView loginByOAuth2CodePipedrive() {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("pipedrive");

        URI redirectUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/settings/marketplace/app/{clientId}/app-settings")
                .build(clientRegistration.getClientId());

        return new RedirectView(redirectUrl.toString());
    }

    @DeleteMapping("oauth2/code/pipedrive")
    @Transactional
    public void uninstall(@RequestBody UninstallRequest request) {
        Client client = clientService.findById(request.getCompanyId());

        if (client.getSystemUser() != null) {
            clientService.unsetSystemUser(request.getCompanyId());
        }

        userService.deleteByUserIdAndClientId(request.getUserId(), request.getCompanyId());
    }
}
