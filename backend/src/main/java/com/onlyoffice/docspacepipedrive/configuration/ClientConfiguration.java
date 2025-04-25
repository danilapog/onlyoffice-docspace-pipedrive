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

package com.onlyoffice.docspacepipedrive.configuration;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.filter.DocspaceAuthorizationApiKeyExchangeFilterFunction;
import com.onlyoffice.docspacepipedrive.client.docspace.filter.DocspaceAuthorizationExchangeFilterFunction;
import com.onlyoffice.docspacepipedrive.client.docspace.impl.DocspaceClientImpl;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class ClientConfiguration {
    @Bean
    DocspaceClient docspaceClient(final DocspaceAccountService docspaceAccountService) {
        DocspaceAuthorizationExchangeFilterFunction docspaceAuthorizationExchangeFilterFunction =
                new DocspaceAuthorizationExchangeFilterFunction(docspaceAccountService);

        WebClient webClient = WebClient.builder()
                .filter(docspaceAuthorizationExchangeFilterFunction)
                .build();

        return new DocspaceClientImpl(webClient);
    }

    @Bean
    DocspaceClient applicationDocspaceClient() {
        DocspaceAuthorizationApiKeyExchangeFilterFunction docspaceAuthorizationApiKeyExchangeFilterFunction =
                new DocspaceAuthorizationApiKeyExchangeFilterFunction();

        WebClient webClient = WebClient.builder()
                .filter(docspaceAuthorizationApiKeyExchangeFilterFunction)
                .build();

        return new DocspaceClientImpl(webClient);
    }

    @Bean
    WebClient pipedriveWebClient(final OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        servletOAuth2AuthorizedClientExchangeFilterFunction.setDefaultClientRegistrationId("pipedrive");
        servletOAuth2AuthorizedClientExchangeFilterFunction.setDefaultOAuth2AuthorizedClient(true);

        return WebClient.builder()
                .filter(servletOAuth2AuthorizedClientExchangeFilterFunction)
                .build();
    }
}
