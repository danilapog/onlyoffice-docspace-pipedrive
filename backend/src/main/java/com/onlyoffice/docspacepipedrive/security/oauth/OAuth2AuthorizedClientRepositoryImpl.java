package com.onlyoffice.docspacepipedrive.security.oauth;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.user.AccessToken;
import com.onlyoffice.docspacepipedrive.entity.user.RefreshToken;
import com.onlyoffice.docspacepipedrive.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OAuth2AuthorizedClientRepositoryImpl implements OAuth2AuthorizedClientRepository {
    @Value("${spring.security.oauth2.client.provider.pipedrive.nested-user-name-attribute}")
    private String userNameAttribute;
    @Value("${spring.security.oauth2.client.provider.pipedrive.nested-client-id-attribute}")
    private String clientIdAttribute;

    private final UserService userService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
                                                                     Authentication authentication,
                                                                     HttpServletRequest request) {
        OAuth2AuthenticatedPrincipal oAuth2User = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

        Long userId = ((Number) oAuth2User.getAttributes().get(userNameAttribute)).longValue();
        Long clientId = ((Number) oAuth2User.getAttributes().get(clientIdAttribute)).longValue();

        User user = userService.findByUserIdAndClientId(userId, clientId);

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
        OAuth2AuthenticatedPrincipal oAuth2User = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();

        Long userId = ((Number) oAuth2User.getAttributes().get(userNameAttribute)).longValue();
        Long clientId = ((Number) oAuth2User.getAttributes().get(clientIdAttribute)).longValue();

        AccessToken accessToken = AccessToken.builder()
                .value(authorizedClient.getAccessToken().getTokenValue())
                .issuedAt(authorizedClient.getAccessToken().getIssuedAt())
                .expiresAt(authorizedClient.getAccessToken().getExpiresAt())
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .value(authorizedClient.getRefreshToken().getTokenValue())
                .issuedAt(authorizedClient.getRefreshToken().getIssuedAt())
                .build();

        User user = User.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        userService.put(clientId, user);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal,
                                       HttpServletRequest request, HttpServletResponse response) {
    }
}
