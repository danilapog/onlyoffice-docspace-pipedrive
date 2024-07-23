package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceAuthentication;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveAccessDeniedException;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import com.onlyoffice.docspacepipedrive.web.aop.docspace.DocspaceAction;
import com.onlyoffice.docspacepipedrive.web.aop.docspace.ExecuteDocspaceAction;
import com.onlyoffice.docspacepipedrive.web.aop.pipedrive.ExecutePipedriveAction;
import com.onlyoffice.docspacepipedrive.web.aop.pipedrive.PipedriveAction;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountRequest;
import com.onlyoffice.docspacepipedrive.web.dto.user.UserResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final ClientService clientService;
    private final DocspaceAccountService docspaceAccountService;
    private final UserMapper userMapper;
    private final PipedriveClient pipedriveClient;
    private final DocspaceClient docspaceClient;

    @GetMapping
    public ResponseEntity<UserResponse> getUser() {
        User currentUser = SecurityUtils.getCurrentUser();

        PipedriveUser pipedriveUser = pipedriveClient.getUser();

        DocspaceUser docspaceUser = null;
        try {
            docspaceUser = docspaceClient.getUser(currentUser.getDocspaceAccount().getUuid());
        } catch (Exception e) {
            //ToDo
        }

        return ResponseEntity.ok(
                userMapper.userToUserResponse(currentUser, pipedriveUser, docspaceUser)
        );
    }

    @PutMapping(path = "/docspace-account", params = "system=false")
    @Transactional
    @ExecuteDocspaceAction(action = DocspaceAction.INVITE_CURRENT_USER_TO_SHARED_GROUP, execution = Execution.AFTER)
    public ResponseEntity<Void> putDocspaceAccount(@RequestBody DocspaceAccountRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        DocspaceUser docspaceUser = SecurityUtils.runAs(new SecurityUtils.RunAsWork<DocspaceUser>() {
            public DocspaceUser doWork() {
                return docspaceClient.getUser(request.getUserName());
            }
        }, currentUser.getClient().getSystemUser());

        DocspaceAccount docspaceAccount = DocspaceAccount.builder()
                .uuid(docspaceUser.getId())
                .email(docspaceUser.getEmail())
                .passwordHash(request.getPasswordHash())
                .build();

        DocspaceAccount savedDocspaceAccount = docspaceAccountService.save(currentUser.getId(), docspaceAccount);
        currentUser.setDocspaceAccount(savedDocspaceAccount);

        return ResponseEntity.ok(null);
    }


    @PutMapping(path = "/docspace-account", params = "system=true")
    @Transactional
    @ExecuteDocspaceAction(action = DocspaceAction.INIT_SHARED_GROUP, execution = Execution.AFTER)
    @ExecutePipedriveAction(action = PipedriveAction.INIT_WEBHOOKS, execution = Execution.AFTER)
    public ResponseEntity<Void> putSystemDocspaceAccount(@RequestBody DocspaceAccountRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

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

        Client client = currentUser.getClient();
        client.setSystemUser(currentUser);
        Client updatedClient = clientService.update(client);

        currentUser.setDocspaceAccount(savedDocspaceAccount);
        currentUser.setClient(updatedClient);

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/docspace-account")
    @Transactional
    @ExecuteDocspaceAction(action = DocspaceAction.REMOVE_CURRENT_USER_FROM_SHARED_GROUP, execution = Execution.BEFORE)
    @ExecutePipedriveAction(action = PipedriveAction.REMOVE_WEBHOOKS, execution = Execution.AFTER)
    public ResponseEntity<Void> deleteDocspaceAccount() {
        User currentUser = SecurityUtils.getCurrentUser();

        docspaceAccountService.deleteById(currentUser.getId());

        if (currentUser.isSystemUser()) {
            clientService.unsetSystemUser(currentUser.getClient().getId());
        }

        return ResponseEntity.noContent().build();
    }
}
