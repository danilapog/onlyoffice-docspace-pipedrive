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

package com.onlyoffice.docspacepipedrive.client.docspace.filter;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.settings.ApiKey;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;

public class DocspaceAuthorizationApiKeyExchangeFilterFunction implements ExchangeFilterFunction {
    @Override
    public Mono<ClientResponse> filter(final ClientRequest request, final ExchangeFunction next) {
        Client client = SecurityUtils.getCurrentClient();
        Settings settings = client.getSettings();
        ApiKey apiKey = settings.getApiKey();

        UriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(settings.getUrl());
        UriBuilder uriBuilder = uriBuilderFactory.uriString(request.url().toString());
        URI uri = uriBuilder.build();

        return next.exchange(
                ClientRequest.from(request)
                        .headers(headers -> {
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.setBearerAuth(apiKey.getValue());
                        })
                        .url(uri)
                        .build()
        );
    }
}
