package com.onlyoffice.docspacepipedrive.client.docspace;

import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceGroup;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceResponse;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceRoom;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceUser;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceWebClientResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DocspaceClient {
    private final WebClient docspaceWebClient;

    public DocspaceUser getUser(String email) {
        return getUser(email, null);
    }

    public DocspaceUser getUser(String email, Settings settings) {
        return docspaceWebClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/api/2.0/people/email")
                            .queryParam("email", email)
                            .build();
                })
                .attributes(attributes -> {
                    if (settings != null) {
                        attributes.put(Settings.class.getName(), settings);
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceUser>>() {})
                .map(DocspaceResponse<DocspaceUser>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceUser getUser(UUID id) {
        return docspaceWebClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/api/2.0/people/{id}")
                            .build(id);
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceUser>>() {})
                .map(DocspaceResponse<DocspaceUser>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceRoom createRoom(String title, Integer roomType) {
        Map<String, Object> map = new HashMap<>();

        map.put("title", title);
        map.put("roomType", roomType);

        return docspaceWebClient.post()
                .uri( "/api/2.0/files/rooms")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceRoom>>() {})
                .map(DocspaceResponse<DocspaceRoom>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceGroup createGroup(String name, UUID owner, List<UUID> members) {
        Map<String, Object> map = new HashMap<>();

        map.put("groupName", name);
        map.put("groupManager", owner);
        map.put("members", members);

        return docspaceWebClient.post()
                .uri( "/api/2.0/group")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceGroup>>() {})
                .map(DocspaceResponse<DocspaceGroup>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceGroup addMembersToGroup(UUID groupId, List<UUID> members) {
        Map<String, Object> map = new HashMap<>();

        map.put("membersToAdd", members);

        return docspaceWebClient.put()
                .uri( uriBuilder -> {
                    return uriBuilder.path("/api/2.0/group/{groupId}")
                            .build(groupId);
                })
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceGroup>>() {})
                .map(DocspaceResponse<DocspaceGroup>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }

    public DocspaceGroup removeMembersFromGroup(UUID groupId, List<UUID> members) {
        Map<String, Object> map = new HashMap<>();

        map.put("membersToRemove", members);

        return docspaceWebClient.put()
                .uri( uriBuilder -> {
                    return uriBuilder.path("/api/2.0/group/{groupId}")
                            .build(groupId);
                })
                .bodyValue(map)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DocspaceResponse<DocspaceGroup>>() {})
                .map(DocspaceResponse<DocspaceGroup>::getResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new DocspaceWebClientResponseException(e));
                })
                .block();
    }
}
