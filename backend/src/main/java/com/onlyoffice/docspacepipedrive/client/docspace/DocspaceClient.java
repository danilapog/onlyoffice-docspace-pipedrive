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

package com.onlyoffice.docspacepipedrive.client.docspace;

import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomInvitationRequest;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceAuthentication;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceGroup;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceMembers;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceResponse;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoom;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceUser;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceWebClientResponseException;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DocspaceClient {
    private static final int PAGINATION_COUNT = 100;
    private final WebClient docspaceWebClient;

    public DocspaceAuthentication login(final String userName, final String passwordHash) {
        User user = SecurityUtils.getCurrentUser();

        Map<String, String> map = new HashMap<>();

        map.put("userName", userName);
        map.put("passwordHash", passwordHash);

        return WebClient.builder().build().post()
                .uri(user.getClient().getSettings().getUrl() + "/api/2.0/authentication")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceAuthentication>>() { })
                .map(DocspaceResponse<DocspaceAuthentication>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceUser getUser(final String email) {
        return docspaceWebClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/api/2.0/people/email")
                            .queryParam("email", email)
                            .build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceUser>>() { })
                .map(DocspaceResponse<DocspaceUser>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceUser getUser(final UUID id) {
        return docspaceWebClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/api/2.0/people/{id}")
                            .build(id);
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceUser>>() { })
                .map(DocspaceResponse<DocspaceUser>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public List<DocspaceUser> findUsers(final Integer employeeType) {
        List<DocspaceUser> docspaceUsers = new ArrayList<>();

        boolean moreItemInCollection = true;
        Integer startIndex = 0;
        Integer count = PAGINATION_COUNT;

        while (moreItemInCollection) {
             DocspaceResponse<List<DocspaceUser>> response = docspaceWebClient.get()
                    .uri(UriComponentsBuilder.fromUriString("")
                            .path("/api/2.0/people/simple/filter")
                            .queryParam("employeeType", employeeType)
                            .queryParam("startIndex", startIndex)
                            .queryParam("count", count)
                            .build()
                            .toUriString()
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<List<DocspaceUser>>>() {
                    })
                    .onErrorResume(WebClientResponseException.class, e -> {
                        return Mono.error(new DocspaceWebClientResponseException(e));
                    })
                    .block();

            docspaceUsers.addAll(response.getResponse());

            startIndex = startIndex + count;
            if (response.getTotal() <= startIndex) {
                moreItemInCollection = false;
            }
        }

        return docspaceUsers;
    }

    public DocspaceRoom createRoom(final String title, final Integer roomType) {
        Map<String, Object> map = new HashMap<>();

        map.put("title", title);
        map.put("roomType", roomType);

        return docspaceWebClient.post()
                .uri("/api/2.0/files/rooms")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceRoom>>() { })
                .map(DocspaceResponse<DocspaceRoom>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceMembers shareRoom(final Long roomId,
                                     final DocspaceRoomInvitationRequest docspaceRoomInvitationRequest) {
        return docspaceWebClient.put()
                .uri(uriBuilder -> {
                    return uriBuilder.path("api/2.0/files/rooms/{roomId}/share")
                            .build(roomId);
                })
                .bodyValue(docspaceRoomInvitationRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceMembers>>() { })
                .map(DocspaceResponse<DocspaceMembers>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceRoom addTagsToRoom(final Long roomId, final List<String> tagNames) {
        Map<String, Object> map = new HashMap<>();
        map.put("names", tagNames);

        return docspaceWebClient.put()
                .uri(uriBuilder -> {
                    return uriBuilder.path("api/2.0/files/rooms/{roomId}/tags")
                            .build(roomId);
                })
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceRoom>>() { })
                .map(DocspaceResponse<DocspaceRoom>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceGroup createGroup(final String name, final UUID owner, final List<UUID> members) {
        Map<String, Object> map = new HashMap<>();

        map.put("groupName", name);
        map.put("groupManager", owner);
        map.put("members", members);

        return docspaceWebClient.post()
                .uri("/api/2.0/group")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceGroup>>() { })
                .map(DocspaceResponse<DocspaceGroup>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceGroup updateGroup(final UUID groupId, final String groupName, final UUID groupManager,
                                     final List<UUID> membersToAdd, final List<UUID> membersToRemove) {
        Map<String, Object> map = new HashMap<>();

        if (groupId != null) {
            map.put("groupName", groupName);
        }
        if (groupManager != null) {
            map.put("groupManager", groupManager);
        }
        if (membersToAdd != null) {
            map.put("membersToAdd", membersToAdd);
        }
        if (membersToRemove != null) {
            map.put("membersToRemove", membersToRemove);
        }

        return docspaceWebClient.put()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/api/2.0/group/{groupId}")
                            .build(groupId);
                })
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceGroup>>() { })
                .map(DocspaceResponse<DocspaceGroup>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public List<String> getTags() {
        return docspaceWebClient.get()
                .uri("api/2.0/files/tags")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<List<String>>>() { })
                .map(DocspaceResponse<List<String>>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public String createTag(final String tagName) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", tagName);

        return docspaceWebClient.post()
                .uri("api/2.0/files/tags")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<String>>() { })
                .map(DocspaceResponse<String>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }
}
