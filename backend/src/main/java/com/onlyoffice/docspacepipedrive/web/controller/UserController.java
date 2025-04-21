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

package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.events.user.DocspaceLoginUserEvent;
import com.onlyoffice.docspacepipedrive.events.user.DocspaceLogoutUserEvent;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceAccessDeniedException;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountRequest;
import com.onlyoffice.docspacepipedrive.web.dto.user.UserResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final ClientService clientService;
    private final DocspaceAccountService docspaceAccountService;
    private final UserMapper userMapper;
    private final PipedriveClient pipedriveClient;
    private final DocspaceClient applicationDocspaceClient;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal User currentUser,
                                                @AuthenticationPrincipal(expression = "client") Client currentClient) {
        PipedriveUser pipedriveUser = pipedriveClient.getUser();

        UriComponents clientUri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(pipedriveUser.getCompanyDomain() + ".pipedrive.com")
                .build();

        boolean updateClient = false;
        if (!clientUri.toUriString().equals(currentClient.getUrl())) {
            currentClient.setUrl(clientUri.toUriString());
            updateClient = true;
        }

        if (!pipedriveUser.getCompanyName().equals(currentClient.getCompanyName())) {
            currentClient.setCompanyName(pipedriveUser.getCompanyName());
            updateClient = true;
        }

        if (updateClient) {
            Client updatedClient = clientService.update(currentClient);
            currentUser.setClient(updatedClient);
        }

        return ResponseEntity.ok(
                userMapper.userToUserResponse(currentUser, pipedriveUser, currentUser.getDocspaceAccount())
        );
    }

    @PutMapping(path = "/docspace-account")
    @Transactional
    public ResponseEntity<Void> putDocspaceAccount(@AuthenticationPrincipal User currentUser,
                                                   @RequestBody DocspaceAccountRequest request) {
        DocspaceUser docspaceUser = applicationDocspaceClient.getUser(request.getUserName());

        if (docspaceUser.getIsVisitor()) {
            throw new DocspaceAccessDeniedException(request.getUserName());
        }

        DocspaceAccount docspaceAccount = DocspaceAccount.builder()
                .uuid(docspaceUser.getId())
                .email(docspaceUser.getEmail())
                .passwordHash(request.getPasswordHash())
                .build();

        DocspaceAccount savedDocspaceAccount = docspaceAccountService.save(currentUser.getId(), docspaceAccount);
        currentUser.setDocspaceAccount(savedDocspaceAccount);

        eventPublisher.publishEvent(new DocspaceLoginUserEvent(this, savedDocspaceAccount));

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/docspace-account")
    public ResponseEntity<Void> deleteDocspaceAccount(@AuthenticationPrincipal User currentUser) {
        docspaceAccountService.deleteById(currentUser.getId());

        eventPublisher.publishEvent(new DocspaceLogoutUserEvent(this, currentUser.getDocspaceAccount()));

        return ResponseEntity.noContent().build();
    }
}
