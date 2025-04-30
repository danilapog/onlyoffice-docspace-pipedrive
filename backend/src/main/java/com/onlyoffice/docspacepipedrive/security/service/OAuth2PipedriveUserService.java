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

package com.onlyoffice.docspacepipedrive.security.service;

import com.onlyoffice.docspacepipedrive.security.oauth.OAuth2PipedriveUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2PipedriveUserService {
    private final WebClient pipedriveWebClient;

    public OAuth2PipedriveUser loadUser(final Long clientId, final Long userId) {
        Map<String, Object> response = pipedriveWebClient.get()
                .uri("https://api.pipedrive.com/v1/users/me")
                .attribute(Authentication.class.getName(), createAuthentication(clientId + ":" + userId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() { })
                .block();

        return new OAuth2PipedriveUser((Map<String, Object>) response.get("data"), null);
    }

    private static Authentication createAuthentication(final String principalName) {
        return new AbstractAuthenticationToken((Collection) null) {
            public Object getCredentials() {
                return "";
            }

            public Object getPrincipal() {
                return principalName;
            }
        };
    }
}
