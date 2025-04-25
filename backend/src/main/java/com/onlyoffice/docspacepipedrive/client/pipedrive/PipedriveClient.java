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

package com.onlyoffice.docspacepipedrive.client.pipedrive;

import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveDeal;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveDealFollower;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveDealFollowerEvent;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveResponse;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUserSettings;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveWebhook;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveOAuth2AuthorizationException;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveWebClientResponseException;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class PipedriveClient {
    private static final int PAGINATION_LIMIT = 100;

    @Value("${pipedrive.base-api-url}")
    private String baseApiUrl;

    private final WebClient pipedriveWebClient;

    public PipedriveDeal getDeal(final Long id) {
        return pipedriveWebClient.get()
                .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                        .path("/v1/deals/{id}")
                        .build(id)
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PipedriveResponse<PipedriveDeal>>() { })
                .map(PipedriveResponse<PipedriveDeal>::getData)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new PipedriveWebClientResponseException(e));
                })
                .block();
    }

    public List<PipedriveDealFollower> getDealFollowers(final Long id) {
        List<PipedriveDealFollower> dealFollowers = new ArrayList<>();

        boolean moreItemInCollection = true;
        Integer start = 0;
        Integer limit = PAGINATION_LIMIT;

        while (moreItemInCollection) {
            PipedriveResponse<List<PipedriveDealFollower>> response = pipedriveWebClient.get()
                    .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                            .path("/v1/deals/{id}/followers")
                            .queryParam("start", start)
                            .queryParam("limit", limit)
                            .build(id)
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PipedriveResponse<List<PipedriveDealFollower>>>() { })
                    .onErrorResume(WebClientResponseException.class, e -> {
                        return Mono.error(new PipedriveWebClientResponseException(e));
                    })
                    .block();

            dealFollowers.addAll(response.getData());

            moreItemInCollection = response.getAdditionalData().getPagination().getMoreItemsInCollection();
            if (moreItemInCollection) {
                start = response.getAdditionalData().getPagination().getNextStart();
            }
        }

        return dealFollowers;
    }

    public List<PipedriveDealFollowerEvent> getDealFollowersFlow(final Long id) {
        List<PipedriveDealFollowerEvent> followers = new ArrayList<>();

        boolean moreItemInCollection = true;
        Integer start = 0;
        Integer limit = PAGINATION_LIMIT;

        while (moreItemInCollection) {
            PipedriveResponse<List<PipedriveDealFollowerEvent>> response = pipedriveWebClient.get()
                    .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                            .path("/v1/deals/{id}/flow")
                            .queryParam("start", start)
                            .queryParam("limit", limit)
                            .queryParam("items", "dealFollower")
                            .build(id)
                    )
                    .retrieve()
                    .bodyToMono(
                            new ParameterizedTypeReference<PipedriveResponse<List<PipedriveDealFollowerEvent>>>() { }
                    )
                    .onErrorResume(WebClientResponseException.class, e -> {
                        return Mono.error(new PipedriveWebClientResponseException(e));
                    })
                    .block();

            followers.addAll(response.getData());

            moreItemInCollection = response.getAdditionalData().getPagination().getMoreItemsInCollection();
            if (moreItemInCollection) {
                start = response.getAdditionalData().getPagination().getNextStart();
            }
        }

        return followers;
    }

    public PipedriveUser getUser() {
        return pipedriveWebClient.get()
                .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                        .path("/v1/users/me")
                        .build()
                        .toUri()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PipedriveResponse<PipedriveUser>>() { })
                .map(PipedriveResponse<PipedriveUser>::getData)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new PipedriveWebClientResponseException(e));
                })
                .onErrorResume(OAuth2AuthorizationException.class, e -> {
                    return Mono.error(new PipedriveOAuth2AuthorizationException(e));
                })
                .block();
    }

    public List<PipedriveUser> getUsers() {
        List<PipedriveUser> users = new ArrayList<>();

        boolean moreItemInCollection = true;
        Integer start = 0;
        Integer limit = PAGINATION_LIMIT;

        while (moreItemInCollection) {
            PipedriveResponse<List<PipedriveUser>> response = pipedriveWebClient.get()
                    .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                            .path("/v1/users")
                            .queryParam("start", start)
                            .queryParam("limit", limit)
                            .build()
                            .toUri()
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PipedriveResponse<List<PipedriveUser>>>() { })
                    .onErrorResume(WebClientResponseException.class, e -> {
                        return Mono.error(new PipedriveWebClientResponseException(e));
                    })
                    .block();

            users.addAll(response.getData());

            try {
                moreItemInCollection = response.getAdditionalData()
                        .getPagination()
                        .getMoreItemsInCollection();
            } catch (NullPointerException e) {
                moreItemInCollection = false;
            }

            if (moreItemInCollection) {
                start = response.getAdditionalData()
                        .getPagination()
                        .getNextStart();
            }
        }

        return users;
    }

    public PipedriveUserSettings getUserSettings() {
        return pipedriveWebClient.get()
                .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                        .path("/v1/userSettings")
                        .build()
                        .toUri()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PipedriveResponse<PipedriveUserSettings>>() { })
                .map(PipedriveResponse<PipedriveUserSettings>::getData)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new PipedriveWebClientResponseException(e));
                })
                .onErrorResume(OAuth2AuthorizationException.class, e -> {
                    return Mono.error(new PipedriveOAuth2AuthorizationException(e));
                })
                .block();
    }

    public PipedriveWebhook createWebhook(final PipedriveWebhook pipedriveWebhook) {
        return pipedriveWebClient.post()
                .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                        .path("/v1/webhooks")
                        .build()
                        .toUri()
                )
                .bodyValue(pipedriveWebhook)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PipedriveResponse<PipedriveWebhook>>() { })
                .map(PipedriveResponse<PipedriveWebhook>::getData)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new PipedriveWebClientResponseException(e));
                })
                .onErrorResume(OAuth2AuthorizationException.class, e -> {
                    return Mono.error(new PipedriveOAuth2AuthorizationException(e));
                })
                .block();
    }

    public void deleteWebhook(final Long id) {
        pipedriveWebClient.delete()
                .uri(UriComponentsBuilder.fromUriString(getBaseUrl())
                        .path("/v1/webhooks/{id}")
                        .build(id)
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() { })
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new PipedriveWebClientResponseException(e));
                })
                .onErrorResume(OAuth2AuthorizationException.class, e -> {
                    return Mono.error(new PipedriveOAuth2AuthorizationException(e));
                })
                .block();
    }

    private String getBaseUrl() {
        String clientUrl = SecurityUtils.getCurrentClient().getUrl();

        if (StringUtils.hasText(clientUrl)) {
            return clientUrl;
        }

        return baseApiUrl;
    }
}
