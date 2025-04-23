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

package com.onlyoffice.docspacepipedrive.web.mapper;

import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountResponse;
import com.onlyoffice.docspacepipedrive.web.dto.user.UserResponse;

import java.util.Objects;

public abstract class UserMapperDelegate implements UserMapper {
    @Override
    public UserResponse userToUserResponse(final PipedriveUser pipedriveUser,
                                           final DocspaceAccount docspaceAccount) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(pipedriveUser.getId());
        userResponse.setIsSystem(user.isSystemUser());
        userResponse.setName(pipedriveUser.getName());
        userResponse.setIsAdmin(!pipedriveUser.getAccess().stream()
                .filter(access -> access.getApp().equals("sales") && access.getAdmin())
                .toList().isEmpty());
        userResponse.setLanguage(pipedriveUser.getLanguage());

        if (Objects.nonNull(docspaceAccount)) {
            DocspaceAccountResponse docspaceAccountResponse = new DocspaceAccountResponse(
                    docspaceAccount.getEmail(),
                    docspaceAccount.getPasswordHash()
            );

            userResponse.setDocspaceAccount(docspaceAccountResponse);
        }

        return userResponse;
    }
}
