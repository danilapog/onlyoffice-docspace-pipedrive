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

package com.onlyoffice.docspacepipedrive.manager;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomInvitation;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomInvitationRequest;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceAccess;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceGroup;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.SharedGroupIdNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.SystemUserNotFoundException;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public void initSharedGroup() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (!currentUser.getClient().getSettings().existSharedGroupId()) {
            DocspaceGroup docspaceGroup = docspaceClient.createGroup(
                    MessageFormat.format("Pipedrive Users ({0})", currentUser.getClient().getUrl()),
                    currentUser.getDocspaceAccount().getUuid(),
                    null
            );

            settingsService.saveSharedGroup(currentUser.getClient().getId(), docspaceGroup.getId());
        } else {
            docspaceClient.updateGroup(
                    currentUser.getClient().getSettings().getSharedGroupId(),
                    null,
                    currentUser.getDocspaceAccount().getUuid(),
                    null,
                    null
            );
        }
    }

    public void inviteCurrentUserToSharedGroup() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getClient().getSettings().existSharedGroupId()) {
            SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
                public Void doWork() {
                    docspaceClient.updateGroup(
                            currentUser.getClient().getSettings().getSharedGroupId(),
                            null,
                            null,
                            Collections.singletonList(currentUser.getDocspaceAccount().getUuid()),
                            null
                    );

                    return null;
                }
            }, currentUser.getClient().getSystemUser());
        } //ToDo: do something if shared group is null
    }


    public void removeCurrentUserFromSharedGroup() {
        User currentUser = SecurityUtils.getCurrentUser();

        try {
            SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
                public Void doWork() {
                    docspaceClient.updateGroup(
                            currentUser.getClient().getSettings().getSharedGroupId(),
                            null,
                            null,
                            null,
                            Collections.singletonList(currentUser.getDocspaceAccount().getUuid())
                    );

                    return null;
                }
            }, currentUser.getClient().getSystemUser());
        } catch (SystemUserNotFoundException | SharedGroupIdNotFoundException e) {
            log.warn(
                    MessageFormat.format(
                            "An attempt to remove a User({0}, {1}) from a shared group failed with the error: {2}",
                            currentUser.getClient().getId().toString(),
                            currentUser.getUserId().toString(),
                            e.getMessage()
                    )
            );
        }
    }

    public void inviteSharedGroupToRoom(final Long roomId) {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getClient().getSettings().existSharedGroupId()) {
            DocspaceRoomInvitation docspaceRoomInvitation =
                    new DocspaceRoomInvitation(
                            currentUser.getClient().getSettings().getSharedGroupId(),
                            DocspaceAccess.EDITING
                    );

            DocspaceRoomInvitationRequest docspaceRoomInvitationRequest = DocspaceRoomInvitationRequest.builder()
                    .invitations(Collections.singletonList(docspaceRoomInvitation))
                    .message("Invitation message")
                    .notify(true)
                    .build();

            docspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
        } //ToDo: do something if shared group is null
    }

    public void removeSharedGroupFromRoom(final Long roomId) {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getClient().getSettings().existSharedGroupId()) {
            DocspaceRoomInvitation docspaceRoomInvitation =
                    new DocspaceRoomInvitation(
                            currentUser.getClient().getSettings().getSharedGroupId(),
                            DocspaceAccess.NONE
                    );

            DocspaceRoomInvitationRequest docspaceRoomInvitationRequest = DocspaceRoomInvitationRequest.builder()
                    .invitations(Collections.singletonList(docspaceRoomInvitation))
                    .notify(false)
                    .build();

            docspaceClient.shareRoom(roomId, docspaceRoomInvitationRequest);
        } //ToDo: do something if shared group is null
    }

    public void inviteListDocspaceAccountsToRoom(final Long roomId, final List<DocspaceAccount> docspaceAccounts) {
        List<UUID> docspaceUnpaidUsers = docspaceClient.findUsers(2) //employeeType 2 = User
                .stream()
                .map(docspaceUser -> {
                    return docspaceUser.getId();
                })
                .toList();

        List<DocspaceRoomInvitation> invitations = docspaceAccounts.stream()
                .map(docspaceAccount -> {
                    DocspaceAccess docspaceAccess = DocspaceAccess.COLLABORATOR;
                    if (docspaceUnpaidUsers.contains(docspaceAccount.getUuid())) {
                        docspaceAccess = DocspaceAccess.EDITING;
                    }

                    return new DocspaceRoomInvitation(
                            docspaceAccount.getUuid(),
                            docspaceAccess
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
