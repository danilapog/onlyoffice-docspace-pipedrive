package com.onlyoffice.docspacepipedrive.web.mapper;

import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountResponse;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import com.onlyoffice.docspacepipedrive.web.dto.user.UserResponse;


public abstract class UserMapperDelegate implements UserMapper {
    @Override
    public UserResponse userToUserResponse(User user, PipedriveUser pipedriveUser, DocspaceUser docspaceUser) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getUserId());
        userResponse.setSystem(user.isSystemUser());
        userResponse.setName(pipedriveUser.getName());
        userResponse.setIsAdmin(pipedriveUser.getAccess().stream()
                .filter(access -> access.getApp().equals("sales") && access.getAdmin())
                .toList().size() > 0);
        userResponse.setLanguage(pipedriveUser.getLanguage());

        if (user.getDocspaceAccount() != null && docspaceUser != null) {
            DocspaceAccountResponse docspaceAccountResponse = new DocspaceAccountResponse(
                    docspaceUser.getEmail(),
                    user.getDocspaceAccount().getPasswordHash(),
                    docspaceUser.getIsOwner() || docspaceUser.getIsAdmin() || docspaceUser.getIsRoomAdmin()
            );

            userResponse.setDocspaceAccount(docspaceAccountResponse);
        }

        if (user.getClient().getSettings() != null) {
            SettingsResponse settingsResponse = new SettingsResponse(
                    user.getClient().getSettings().getUrl()
            );

            userResponse.setDocspaceSettings(settingsResponse);
        }

        return userResponse;
    }
}
