package com.onlyoffice.docspacepipedrive.security.oauth;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {
    @Value("${spring.security.oauth2.client.provider.pipedrive.nested-user-name-attribute}")
    private String userNameAttribute;
    @Value("${spring.security.oauth2.client.provider.pipedrive.nested-client-id-attribute}")
    private String clientIdAttribute;
    @Value("${spring.security.oauth2.client.provider.pipedrive.nested-client-url-attribute}")
    private String clientUrlAttribute;

    private final ClientService clientService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        String userNestedNameAttribute = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Map<String, Object> attributesFromNestedObject = (Map<String, Object>) user.getAttribute(userNestedNameAttribute);

        if (!attributesFromNestedObject.containsKey(userNameAttribute)) {
            throw new IllegalArgumentException("Missing attribute '" + userNameAttribute + "' in attributes");
        } else {
            Long clientId = ((Number) attributesFromNestedObject.get(clientIdAttribute)).longValue();
            String clientUrl = (String) userRequest.getAdditionalParameters().get(clientUrlAttribute);

            Client client = Client.builder()
                    .id(clientId)
                    .url(clientUrl)
                    .build();

            if (clientService.existById(clientId)) {
                clientService.update(client);
            } else {
                clientService.create(client);
            }

            Set<GrantedAuthority> authorities = user.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().equals("OAUTH2_USER")
                            ?  new OAuth2UserAuthority(attributesFromNestedObject)
                            : grantedAuthority
                    ).collect(Collectors.toSet());

            return new DefaultOAuth2User(authorities, attributesFromNestedObject, userNameAttribute);
        }
    }
}
