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
import com.onlyoffice.docspacepipedrive.client.docspace.impl.DocspaceClientImpl;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;


@Configuration
public class ClientConfiguration {
    @Bean
    DocspaceClient docspaceClient(final SettingsService settingsService) {
        DocspaceAuthorizationApiKeyExchangeFilterFunction docspaceAuthorizationApiKeyExchangeFilterFunction =
                new DocspaceAuthorizationApiKeyExchangeFilterFunction(settingsService);

        WebClient webClient = WebClient.builder()
                .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                .filter(docspaceBaseUrlFilter())
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

    private ExchangeFilterFunction docspaceBaseUrlFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            Client currentClient = SecurityUtils.getCurrentClient();
            Settings settings = currentClient.getSettings();

            UriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(settings.getUrl());
            UriBuilder uriBuilder = uriBuilderFactory.uriString(request.url().toString());
            URI uri = uriBuilder.build();

            ClientRequest newRequest = ClientRequest.from(request)
                    .url(uri)
                    .build();

            return Mono.just(newRequest);
        });
    }
}
