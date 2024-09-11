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

package com.onlyoffice.docspacepipedrive.security.oauth;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.user.AccessToken;
import com.onlyoffice.docspacepipedrive.entity.user.RefreshToken;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OAuth2AuthorizedClientRepositoryImpl implements OAuth2AuthorizedClientRepository {
    private final ClientService clientService;
    private final UserService userService;
    private final SettingsService settingsService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(final String clientRegistrationId,
                                                                     final Authentication authentication,
                                                                     final HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                user.getAccessToken().getValue(),
                user.getAccessToken().getIssuedAt(),
                user.getAccessToken().getExpiresAt(),
                null
        );

        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                user.getRefreshToken().getValue(),
                user.getRefreshToken().getIssuedAt(),
                null
        );

        return (T) new OAuth2AuthorizedClient(
                clientRegistrationRepository.findByRegistrationId(clientRegistrationId),
                user.getId().toString(),
                accessToken,
                refreshToken
        );
    }

    @Override
    public void saveAuthorizedClient(final OAuth2AuthorizedClient authorizedClient, final Authentication authentication,
                                     final HttpServletRequest request, final HttpServletResponse response) {
        AccessToken accessToken = AccessToken.builder()
                .value(authorizedClient.getAccessToken().getTokenValue())
                .issuedAt(authorizedClient.getAccessToken().getIssuedAt())
                .expiresAt(authorizedClient.getAccessToken().getExpiresAt())
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .value(authorizedClient.getRefreshToken().getTokenValue())
                .issuedAt(authorizedClient.getRefreshToken().getIssuedAt())
                .build();

        String[] partsRefreshToken = refreshToken.getValue().split(":");

        Client client = Client.builder()
                .id(Long.parseLong(partsRefreshToken[0]))
                .url("")
                .build();

        if (!clientService.existById(client.getId())) {
            clientService.create(client);
            settingsService.put(client.getId(),
                    Settings.builder().build()
            );
        }

        User user = User.builder()
                .userId(Long.parseLong(partsRefreshToken[1]))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        userService.put(client.getId(), user);
    }

    @Override
    public void removeAuthorizedClient(final String clientRegistrationId, final Authentication authentication,
                                       final HttpServletRequest request, final HttpServletResponse response) {
        User user = (User) authentication.getPrincipal();

        userService.deleteByUserIdAndClientId(user.getUserId(), user.getClient().getId());
    }
}
