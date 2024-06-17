package com.onlyoffice.docspacepipedrive.configuration;

import com.onlyoffice.docspacepipedrive.client.docspace.filter.DocspaceAuthorizationExchangeFilterFunction;
import com.onlyoffice.docspacepipedrive.service.DocspaceTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class ClientConfiguration {
    @Bean
    WebClient docspaceWebClient(DocspaceTokenService docspaceTokenService) {
        DocspaceAuthorizationExchangeFilterFunction docspaceAuthorizationExchangeFilterFunction =
                new DocspaceAuthorizationExchangeFilterFunction(docspaceTokenService);

        return WebClient.builder()
                .filter(docspaceAuthorizationExchangeFilterFunction)
                .build();
    }

    @Bean
    WebClient pipedriveWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        servletOAuth2AuthorizedClientExchangeFilterFunction.setDefaultOAuth2AuthorizedClient(true);

        return WebClient.builder()
                .filter(servletOAuth2AuthorizedClientExchangeFilterFunction)
                .build();
    }
}
