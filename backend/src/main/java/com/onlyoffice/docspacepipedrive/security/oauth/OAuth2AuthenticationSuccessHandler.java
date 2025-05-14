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

package com.onlyoffice.docspacepipedrive.security.oauth;

import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.settings.ApiKey;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceApiKeyNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceUrlNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final PipedriveActionManager pipedriveActionManager;
    private final SettingsService settingsService;
    private final UserService userService;


    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> userData = oAuth2User.getAttribute("data");

        Long userId = ((Integer) userData.get("id")).longValue();
        Long clientId = ((Integer) userData.get("company_id")).longValue();
        String domain = (String) userData.get("company_domain");

        if (isInitializeWebhooks(clientId) && isDealAdmin(userData)) {
            SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
                public Void doWork() {
                    pipedriveActionManager.initWebhooks();
                    return null;
                }
            }, userService.findByClientIdAndUserId(clientId, userId));

        }

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("pipedrive");

        URI redirectUrl = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(domain + ".pipedrive.com")
                .path("/settings/marketplace/app/{clientId}/app-settings")
                .build(clientRegistration.getClientId());

        redirectStrategy.sendRedirect(request, response, redirectUrl.toString());
    }

    private boolean isInitializeWebhooks(final Long clientId) {
        try {
            Settings settings = settingsService.findByClientId(clientId);
            String url = settings.getUrl();
            ApiKey apiKey = settings.getApiKey();

            return !url.isEmpty() && apiKey.isValid() && !pipedriveActionManager.isWebhooksInstalled(clientId);
        } catch (SettingsNotFoundException | DocspaceUrlNotFoundException | DocspaceApiKeyNotFoundException e) {
            return false;
        }
    }

    private boolean isDealAdmin(final Map<String, Object> userData) {
        ArrayList<Object> access = (ArrayList<Object>) userData.get("access");

        return access.stream()
                .anyMatch(accessDataObject -> {
                    Map<String, Object> accessData = (Map<String, Object>) accessDataObject;
                    return accessData.get("app").equals("sales") && accessData.get("admin").equals(true);
                });
    }
}
