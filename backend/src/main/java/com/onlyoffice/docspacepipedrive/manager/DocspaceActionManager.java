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

package com.onlyoffice.docspacepipedrive.manager;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceAccess;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceApiKey;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceCSPSettings;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceGroup;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceMembers;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomInvitation;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomInvitationRequest;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceUser;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.settings.ApiKey;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceWebClientResponseException;
import com.onlyoffice.docspacepipedrive.exceptions.ErrorCode;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsValidationException;
import com.onlyoffice.docspacepipedrive.exceptions.SharedGroupIdNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.SharedGroupIsNotPresentInResponse;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class DocspaceActionManager {
    private final DocspaceClient applicationDocspaceClient;
    private final SettingsService settingsService;
    private final UserService userService;

    public void initSharedGroup() {
        Client currentClient = SecurityUtils.getCurrentClient();
        Settings settings = currentClient.getSettings();
        ApiKey apiKey = settings.getApiKey();

        List<User> users = userService.findAllByClientId(currentClient.getId());
        List<UUID> members = users.stream()
                .filter(user -> user.getDocspaceAccount() != null)
                .map(user -> user.getDocspaceAccount().getUuid())
                .toList();

        if (!settings.existSharedGroupId()) {
            DocspaceGroup docspaceGroup = applicationDocspaceClient.createGroup(
                    MessageFormat.format("Pipedrive Users ({0})", currentClient.getCompanyName()),
                    apiKey.getOwnerId(),
                    members
            );

            Settings savedSetting = settingsService.saveSharedGroup(
                    currentClient.getId(),
                    docspaceGroup.getId()
            );
            currentClient.setSettings(savedSetting);
        } else {
            try {
                applicationDocspaceClient.updateGroup(
                        settings.getSharedGroupId(),
                        null,
                        apiKey.getOwnerId(),
                        members,
                        null
                );
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    Settings savedSetting = settingsService.saveSharedGroup(
                            currentClient.getId(),
                            null
                    );
                    currentClient.setSettings(savedSetting);

                    initSharedGroup();
                    return;
                }

                throw e;
            }
        }
    }

    public void inviteDocspaceAccountToSharedGroup(final UUID docspaceAccountId) {
        Client currentClient = SecurityUtils.getCurrentClient();

        try {
            applicationDocspaceClient.updateGroup(
                    currentClient.getSettings().getSharedGroupId(),
                    null,
                    null,
                    Collections.singletonList(docspaceAccountId),
                    null
            );
        } catch (WebClientResponseException | SharedGroupIdNotFoundException e) {
            if (e instanceof SharedGroupIdNotFoundException) {
                initSharedGroup();
                return;
            }

            if (e instanceof WebClientResponseException
                    && ((WebClientResponseException) e).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                Settings savedSetting = settingsService.saveSharedGroup(
                        currentClient.getId(),
                        null
                );
                currentClient.setSettings(savedSetting);

                initSharedGroup();
            }
        }
    }

    public void removeDocspaceAccountFromSharedGroup(final UUID docspaceAccountId) {
        Client currentClient = SecurityUtils.getCurrentClient();

        applicationDocspaceClient.updateGroup(
                currentClient.getSettings().getSharedGroupId(),
                null,
                null,
                null,
                Collections.singletonList(docspaceAccountId)
        );
    }

    public void inviteSharedGroupToRoom(final Long roomId) {
        Client currentClient = SecurityUtils.getCurrentClient();
        UUID sharedGroupId = currentClient.getSettings().getSharedGroupId();

        DocspaceRoomInvitation docspaceRoomInvitation =
                new DocspaceRoomInvitation(
                        sharedGroupId,
                        DocspaceAccess.COLLABORATOR
                );

        DocspaceRoomInvitationRequest docspaceRoomInvitationRequest = DocspaceRoomInvitationRequest.builder()
                .invitations(Collections.singletonList(docspaceRoomInvitation))
                .message("Invitation message")
                .notify(true)
                .build();

        DocspaceMembers docspaceMembers = applicationDocspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
        boolean sharedGroupIsPresentInResponse = docspaceMembers.getMembers().stream()
                .filter(docspaceMember -> docspaceMember.getSharedTo().getId().equals(sharedGroupId))
                .findFirst()
                .isPresent();

        if (!sharedGroupIsPresentInResponse) {
            throw new SharedGroupIsNotPresentInResponse(sharedGroupId, roomId);
        }
    }

    public void removeSharedGroupFromRoom(final Long roomId) {
        Client currentClient = SecurityUtils.getCurrentClient();
        UUID sharedGroupId = currentClient.getSettings().getSharedGroupId();

        DocspaceRoomInvitation docspaceRoomInvitation =
                new DocspaceRoomInvitation(
                        sharedGroupId,
                        DocspaceAccess.NONE
                );

        DocspaceRoomInvitationRequest docspaceRoomInvitationRequest = DocspaceRoomInvitationRequest.builder()
                .invitations(Collections.singletonList(docspaceRoomInvitation))
                .notify(false)
                .build();

        applicationDocspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
    }

    public void inviteListDocspaceAccountsToRoom(final Long roomId, final List<DocspaceAccount> docspaceAccounts) {
        List<DocspaceRoomInvitation> invitations = docspaceAccounts.stream()
                .map(docspaceAccount -> {
                    return new DocspaceRoomInvitation(
                            docspaceAccount.getUuid(),
                            DocspaceAccess.COLLABORATOR
                    );
                })
                .toList();

        if (invitations.size() > 0) {
            DocspaceRoomInvitationRequest docspaceRoomInvitationRequest = DocspaceRoomInvitationRequest.builder()
                    .invitations(invitations)
                    .message("Invitation message")
                    .notify(true)
                    .build();

            applicationDocspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
        }
    }

    public void removeListDocspaceAccountsFromRoom(final Long roomId, final List<DocspaceAccount> docspaceAccounts) {
        List<DocspaceRoomInvitation> invitations = docspaceAccounts.stream()
                .map(docspaceAccount -> new DocspaceRoomInvitation(
                        docspaceAccount.getUuid(),
                        DocspaceAccess.NONE
                ))
                .toList();

        if (invitations.size() > 0) {
            DocspaceRoomInvitationRequest docspaceRoomInvitationRequest = DocspaceRoomInvitationRequest.builder()
                    .invitations(invitations)
                    .notify(false)
                    .build();

            applicationDocspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
        }
    }

    public Settings validateSettings(final Settings settings) {
        Client client = SecurityUtils.getCurrentClient();
        Settings clientSettings = client.getSettings();
        ApiKey apiKey = settings.getApiKey();

        clientSettings.setUrl(settings.getUrl());
        clientSettings.setApiKey(settings.getApiKey());

        List<DocspaceApiKey> docspaceApiKeys = new ArrayList<>();
        try {
            docspaceApiKeys = applicationDocspaceClient.getApiKeys();
        } catch (WebClientRequestException e) {
            log.warn("Error while getting DocSpace API keys", e);

            throw new SettingsValidationException(ErrorCode.DOCSPACE_CAN_NOT_BE_REACHED);
        } catch (DocspaceWebClientResponseException e) {
            log.warn("Error while getting DocSpace API keys", e);

            throw new SettingsValidationException(ErrorCode.DOCSPACE_API_KEY_IS_INVALID);
        }

        DocspaceApiKey docspaceApiKey = docspaceApiKeys.stream()
                .filter(key -> apiKey.getValue().endsWith(
                        key.getKeyPostfix())
                )
                .findFirst()
                .orElse(null);

        if (Objects.isNull(docspaceApiKey)) {
            throw new SettingsValidationException(ErrorCode.DOCSPACE_API_KEY_IS_INVALID);
        }

        List<String> scopes = docspaceApiKey.getPermissions();
        if (Objects.nonNull(scopes) && !scopes.isEmpty()
                && docspaceApiKey.getIsActive()
                && (!scopes.contains("accounts.self:read")
                    || !scopes.contains("accounts:write")
                    || !scopes.contains("rooms:write")
                )
        ) {
            throw new SettingsValidationException(ErrorCode.DOCSPACE_API_KEY_IS_INVALID);
        }

        DocspaceUser docspaceUser = applicationDocspaceClient.getUser();
        if (!docspaceUser.getIsAdmin()) {
            throw new SettingsValidationException(ErrorCode.DOCSPACE_API_KEY_OWNER_IS_NOT_ADMIN);
        }

        apiKey.setOwnerId(docspaceUser.getId());
        return settings;
    }

    public DocspaceCSPSettings addDomainsToCSPSettings(final List<String> domains) {
        DocspaceCSPSettings docspaceCSPSettings = applicationDocspaceClient.getCSPSettings();

        List<String> allowedDomains = docspaceCSPSettings.getDomains();

        List<String> notAllowedDomains = domains.stream()
                .filter(domain -> !allowedDomains.contains(domain))
                .toList();

        if (!notAllowedDomains.isEmpty()) {
            allowedDomains.addAll(notAllowedDomains);

            return applicationDocspaceClient.updateCSPSettings(allowedDomains);
        }

        return docspaceCSPSettings;
    }

}
