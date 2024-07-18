package com.onlyoffice.docspacepipedrive.security.oauth;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.user.AccessToken;
import com.onlyoffice.docspacepipedrive.entity.user.RefreshToken;
import com.onlyoffice.docspacepipedrive.service.ClientService;
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
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
                                                                     Authentication authentication,
                                                                     HttpServletRequest request) {
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
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication authentication,
                                     HttpServletRequest request, HttpServletResponse response) {
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

        if (clientService.existById(client.getId())) {
            clientService.update(client);
        } else {
            clientService.create(client);
        }

        User user = User.builder()
                .userId(Long.parseLong(partsRefreshToken[1]))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        userService.put(client.getId(), user);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal,
                                       HttpServletRequest request, HttpServletResponse response) {
    }
}
