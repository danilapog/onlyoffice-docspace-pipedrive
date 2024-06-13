package com.onlyoffice.docspacepipedrive.client.docspace.filter;

import com.onlyoffice.docspacepipedrive.entity.DocspaceToken;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.DocspaceTokenService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class DocspaceAuthorizationExchangeFilterFunction implements ExchangeFilterFunction {
    private final UserService userService;
    private final DocspaceTokenService docspaceTokenService;
    private final WebClient webClient;

    public DocspaceAuthorizationExchangeFilterFunction(UserService userService, DocspaceTokenService docspaceTokenService) {
        this.userService = userService;
        this.docspaceTokenService = docspaceTokenService;

        this.webClient = WebClient.builder().build();
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        request = configureRequest(request);

        return exchangeRequest(request, next);
    }

    private Mono<ClientResponse> exchangeRequest(ClientRequest request, ExchangeFunction next) {
        return Mono.defer(() -> {
            return authorize(request)
                    .flatMap((req) -> {
                       return next.exchange(req)
                               .flatMap(clientResponse -> {
                                   if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                       return reauthorize(request).flatMap(next::exchange);
                                   }

                                   return Mono.just(clientResponse);
                               });
                    }).switchIfEmpty(Mono.defer(() -> {
                        return reauthorize(request).flatMap(next::exchange);
                    }));
        });
    }

    private ClientRequest configureRequest(ClientRequest request) {
        Settings settingsFromStorage = userService.findById(SecurityUtils.getCurrentUserId())
                .getClient()
                .getSettings();

        Settings settings = (Settings) request.attribute(Settings.class.getName()).orElse(
                settingsFromStorage
        );
        UriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(settings.getUrl());
        UriBuilder uriBuilder = uriBuilderFactory.uriString(request.url().toString());
        URI uri = uriBuilder.build();

        return ClientRequest.from(request)
                .headers((headers) -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .attributes(attributes -> {
                    boolean reauthorize = false;

                    if (attributes.containsKey(Settings.class.getName())) {
                        if (settingsFromStorage != null
                                && (!settingsFromStorage.getUrl().equals(settings.getUrl())
                                || !settingsFromStorage.getUserName().equals(settings.getUserName())
                                || !settingsFromStorage.getPasswordHash().equals(settings.getPasswordHash()))
                        ) {
                            reauthorize = true;
                        }
                    } else {
                        attributes.put(Settings.class.getName(), settingsFromStorage);
                    }

                    attributes.put("reauthorize", reauthorize);
                })
                .url(uri)
                .build();
    }

    private Mono<ClientRequest> authorize(ClientRequest request) {
        return Mono.defer(() -> {
            boolean reauthorize = (boolean) request.attribute("reauthorize").orElse(false);
            if (reauthorize) {
                return Mono.empty();
            }

            DocspaceToken docspaceToken = docspaceTokenService.findByClientId(getClientId(request));

            if (docspaceToken != null
                    && docspaceToken.getValue() != null
                    && !reauthorize) {
                return Mono.just(setAuthorizationToRequest(request, docspaceToken));
            } else {
                return Mono.empty();
            }
        });
    }

    private Mono<ClientRequest> reauthorize(ClientRequest request) {
        Settings settings = getSettings(request);

        return login(settings).map(token -> {
            return docspaceTokenService.put(
                    settings.getClient().getId(),
                    token
            );
        }).map(docspaceToken -> {
            return setAuthorizationToRequest(request, docspaceToken);
        });
    }

    private Mono<String> login(Settings settings) {
        Map<String, String> map = new HashMap<>();

        map.put("userName", settings.getUserName());
        map.put("passwordHash", settings.getPasswordHash());

        return webClient.post()
                .uri(settings.getUrl() + "/api/2.0/authentication")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(Map.class)
                .transform((responseMono) -> {
                    return responseMono.flatMap(responseMap -> {
                        Map<String, Object> response = (Map<String, Object>) responseMap.get("response");
                        return Mono.just((String) response.get("token"));
                    });
                });
    }

    private ClientRequest setAuthorizationToRequest(ClientRequest request, DocspaceToken docspaceToken) {
        return ClientRequest.from(request)
                .cookie("asc_auth_key", docspaceToken.getValue())
                .build();
    }

    private Settings getSettings(ClientRequest request) {
        return (Settings) request.attribute(Settings.class.getName())
                .orElse(null);
    }

    private Long getClientId(ClientRequest request) {
        return getSettings(request).getClient().getId();
    }
}
