package com.onlyoffice.docspacepipedrive.manager;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.request.DocspaceRoomInvitation;
import com.onlyoffice.docspacepipedrive.client.docspace.request.DocspaceRoomInvitationRequest;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceAccess;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceGroup;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DocspaceActionManager {
    private final DocspaceClient docspaceClient;
    private final SettingsService settingsService;

    public void initSharedGroup() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getClient().getSettings().getSharedGroupId() == null) {
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

        if (currentUser.getClient().getSettings().getSharedGroupId() != null) {
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

        if (currentUser.getClient().getSettings().getSharedGroupId() != null) {
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
        } //ToDo: do something if shared group is null
    }


    public void inviteListDocspaceAccountsToRoom(Long roomId, List<DocspaceAccount> docspaceAccounts) {
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

}
