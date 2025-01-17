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
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceGroup;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceMembers;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomInvitation;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomInvitationRequest;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.SharedGroupIdNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.SharedGroupIsNotPresentInResponse;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class DocspaceActionManager {
    private final DocspaceClient docspaceClient;
    private final SettingsService settingsService;
    private final UserService userService;

    public void initSharedGroup() {
        Client currentClient = SecurityUtils.getCurrentClient();

        User systemUser = currentClient.getSystemUser();

        List<User> users = userService.findAllByClientId(currentClient.getId());
        List<UUID> members = users.stream()
                .filter(user -> user.getDocspaceAccount() != null)
                .map(user -> user.getDocspaceAccount().getUuid())
                .toList();

        SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
            public Void doWork() {
                if (!currentClient.getSettings().existSharedGroupId()) {
                    DocspaceGroup docspaceGroup = docspaceClient.createGroup(
                            MessageFormat.format("Pipedrive Users ({0})", currentClient.getCompanyName()),
                            systemUser.getDocspaceAccount().getUuid(),
                            members
                    );

                    Settings savedSetting = settingsService.saveSharedGroup(
                            currentClient.getId(),
                            docspaceGroup.getId()
                    );
                    currentClient.setSettings(savedSetting);
                } else {
                    try {
                        docspaceClient.updateGroup(
                                currentClient.getSettings().getSharedGroupId(),
                                null,
                                systemUser.getDocspaceAccount().getUuid(),
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
                            return null;
                        }

                        throw e;
                    }
                }

                return null;
            }
        }, systemUser);
    }

    public void inviteDocspaceAccountToSharedGroup(final UUID docspaceAccountId) {
        Client currentClient = SecurityUtils.getCurrentClient();

        try {
            docspaceClient.updateGroup(
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

        docspaceClient.updateGroup(
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

        DocspaceMembers docspaceMembers = docspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
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

        docspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
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

            docspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
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

            docspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
        }
    }

}
