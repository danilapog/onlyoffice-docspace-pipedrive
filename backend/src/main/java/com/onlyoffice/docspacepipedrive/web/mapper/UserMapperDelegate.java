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

package com.onlyoffice.docspacepipedrive.web.mapper;

import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountResponse;
import com.onlyoffice.docspacepipedrive.web.dto.user.UserResponse;


public abstract class UserMapperDelegate implements UserMapper {
    @Override
    public UserResponse userToUserResponse(final User user, final PipedriveUser pipedriveUser,
                                           final DocspaceUser docspaceUser) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getUserId());
        userResponse.setIsSystem(user.isSystemUser());
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

        return userResponse;
    }
}
