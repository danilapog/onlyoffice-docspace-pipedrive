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

package com.onlyoffice.docspacepipedrive.client.docspace.filter;

import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class DocspaceAuthorizationExchangeFilterFunction implements ExchangeFilterFunction {
    private final DocspaceAccountService docspaceAccountService;
    private final WebClient webClient;

    public DocspaceAuthorizationExchangeFilterFunction(final DocspaceAccountService docspaceAccountService) {
        this.docspaceAccountService = docspaceAccountService;

        this.webClient = WebClient.builder().build();
    }

    @Override
    public Mono<ClientResponse> filter(final ClientRequest request, final ExchangeFunction next) {
        return Mono.just(configureRequest(request))
                .flatMap(configuredRequest -> {
                    return authorize(configuredRequest)
                            .flatMap(req -> {
                                return next.exchange(req)
                                        .flatMap(clientResponse -> {
                                            if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return reauthorize(configuredRequest).flatMap(next::exchange);
                                            }

                                            return Mono.just(clientResponse);
                                        });
                            }).switchIfEmpty(Mono.defer(() -> {
                                    return reauthorize(configuredRequest).flatMap(next::exchange);
                            }));
                });
    }

    private ClientRequest configureRequest(final ClientRequest request) {
        User user = SecurityUtils.getCurrentUser();

        UriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(user.getClient().getSettings().getUrl());
        UriBuilder uriBuilder = uriBuilderFactory.uriString(request.url().toString());
        URI uri = uriBuilder.build();

        return ClientRequest.from(request)
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .attributes(attributes -> {
                    attributes.put(User.class.getName(), user);
                })
                .url(uri)
                .build();
    }

    private Mono<ClientRequest> authorize(final ClientRequest request) {
        User user = (User) request.attribute(User.class.getName()).get();

        return Mono.defer(() -> {
            DocspaceAccount docspaceAccount = user.getDocspaceAccount();

            if (docspaceAccount != null
                    && docspaceAccount.getDocspaceToken() != null
                    && docspaceAccount.getDocspaceToken().getValue() != null) {
                return Mono.just(setAuthorizationToRequest(request, docspaceAccount.getDocspaceToken()));
            } else {
                return Mono.empty();
            }
        });
    }

    private Mono<ClientRequest> reauthorize(final ClientRequest request) {
        User user = (User) request.attribute(User.class.getName()).get();

        return login(user).map(token -> {
            return docspaceAccountService.saveToken(
                    user.getId(),
                    token
            );
        }).map(docspaceToken -> {
            user.getDocspaceAccount().setDocspaceToken(docspaceToken);
            return setAuthorizationToRequest(request, docspaceToken);
        }).onErrorResume(WebClientResponseException.class, e -> {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                docspaceAccountService.deleteById(user.getId());
            }

            return Mono.error(e);
        });
    }

    private Mono<String> login(final User user) {
        Map<String, String> map = new HashMap<>();

        map.put("userName", user.getDocspaceAccount().getEmail());
        map.put("passwordHash", user.getDocspaceAccount().getPasswordHash());

        return webClient.post()
                .uri(user.getClient().getSettings().getUrl() + "/api/2.0/authentication")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(Map.class)
                .transform(responseMono -> {
                    return responseMono.flatMap(responseMap -> {
                        Map<String, Object> response = (Map<String, Object>) responseMap.get("response");
                        return Mono.just((String) response.get("token"));
                    });
                });
    }

    private ClientRequest setAuthorizationToRequest(final ClientRequest request, final DocspaceToken docspaceToken) {
        return ClientRequest.from(request)
                .cookie("asc_auth_key", docspaceToken.getValue())
                .build();
    }
}
