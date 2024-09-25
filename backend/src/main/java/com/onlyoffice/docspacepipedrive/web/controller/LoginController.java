/**
 *
 * (c) Copyright Ascensio System SIA 2024
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

package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.entity.User;
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

    @GetMapping("/oauth2/code/pipedrive")
    public RedirectView loginByOAuth2CodePipedrive() {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("pipedrive");

        URI redirectUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/settings/marketplace/app/{clientId}/app-settings")
                .build(clientRegistration.getClientId());

        return new RedirectView(redirectUrl.toString());
    }

    @GetMapping(value = "/oauth2/code/pipedrive", params = "error=user_denied")
    public RedirectView cancelInstall() {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("pipedrive");

        URI redirectUrl = UriComponentsBuilder.fromUriString("https://www.pipedrive.com")
                .path("/marketplace/app/onlyoffice-doc-space/{clientId}")
                .build(clientRegistration.getClientId());

        return new RedirectView(redirectUrl.toString());
    }

    @DeleteMapping("oauth2/code/pipedrive")
    @Transactional
    public void uninstall(@RequestBody UninstallRequest request) {
        User user = userService.findByClientIdAndUserId(request.getCompanyId(), request.getUserId());

        if (user.isSystemUser()) {
            clientService.unsetSystemUser(request.getCompanyId());
        }

        userService.deleteByUserIdAndClientId(request.getUserId(), request.getCompanyId());
    }
}
