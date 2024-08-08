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

package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceAuthentication;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;
import com.onlyoffice.docspacepipedrive.events.user.DocspaceLoginUserEvent;
import com.onlyoffice.docspacepipedrive.events.user.DocspaceLogoutUserEvent;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveAccessDeniedException;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
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
    private final DocspaceClient docspaceClient;
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

        DocspaceUser docspaceUser = null;
        try {
            docspaceUser = docspaceClient.getUser(currentUser.getDocspaceAccount().getUuid());

            if (!currentUser.getDocspaceAccount().getUuid().equals(docspaceUser.getId())) {
                docspaceAccountService.deleteById(currentUser.getId());
                docspaceUser = null;
            }
        } catch (Exception e) {
            //ToDo
        }

        return ResponseEntity.ok(
                userMapper.userToUserResponse(currentUser, pipedriveUser, docspaceUser)
        );
    }

    @PutMapping(path = "/docspace-account", params = "system=false")
    @Transactional
    public ResponseEntity<Void> putDocspaceAccount(@AuthenticationPrincipal User currentUser,
                                                   @AuthenticationPrincipal(expression = "client") Client currentClient,
                                                   @RequestBody DocspaceAccountRequest request) {
        DocspaceUser docspaceUser = SecurityUtils.runAs(new SecurityUtils.RunAsWork<DocspaceUser>() {
            public DocspaceUser doWork() {
                return docspaceClient.getUser(request.getUserName());
            }
        }, currentClient.getSystemUser());

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


    @PutMapping(path = "/docspace-account", params = "system=true")
    @Transactional
    public ResponseEntity<Void> putSystemDocspaceAccount(@AuthenticationPrincipal User currentUser,
                                                         @AuthenticationPrincipal(expression = "client") Client currentClient,
                                                         @RequestBody DocspaceAccountRequest request) {
        PipedriveUser pipedriveUser = pipedriveClient.getUser();
        if (!pipedriveUser.isSalesAdmin()) {
            throw new PipedriveAccessDeniedException(currentUser.getUserId());
        }

        DocspaceAuthentication docspaceAuthentication = docspaceClient.login(
                request.getUserName(),
                request.getPasswordHash()
        );

        DocspaceToken docspaceToken = DocspaceToken.builder()
                .value(docspaceAuthentication.getToken())
                .build();

        DocspaceAccount docspaceAccount = DocspaceAccount.builder()
                .docspaceToken(docspaceToken)
                .build();

        currentUser.setDocspaceAccount(docspaceAccount);

        DocspaceUser docspaceUser = docspaceClient.getUser(request.getUserName());

        if (!docspaceUser.getIsAdmin()) {
            throw new DocspaceAccessDeniedException(request.getUserName());
        }

        docspaceAccount.setUuid(docspaceUser.getId());
        docspaceAccount.setEmail(docspaceUser.getEmail());
        docspaceAccount.setPasswordHash(request.getPasswordHash());

        DocspaceAccount savedDocspaceAccount = docspaceAccountService.save(currentUser.getId(), docspaceAccount);

        currentClient.setSystemUser(currentUser);
        Client updatedClient = clientService.update(currentClient);

        currentUser.setDocspaceAccount(savedDocspaceAccount);
        currentUser.setClient(updatedClient);

        eventPublisher.publishEvent(new DocspaceLoginUserEvent(this, savedDocspaceAccount));

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/docspace-account")
    public ResponseEntity<Void> deleteDocspaceAccount(@AuthenticationPrincipal User currentUser) {

        eventPublisher.publishEvent(new DocspaceLogoutUserEvent(this, currentUser.getDocspaceAccount()));

        docspaceAccountService.deleteById(currentUser.getId());

        return ResponseEntity.noContent().build();
    }
}
