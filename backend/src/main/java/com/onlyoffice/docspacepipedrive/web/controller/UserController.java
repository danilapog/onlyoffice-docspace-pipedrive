package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceAuthentication;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveAccessDeniedException;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.dto.user.UserRequest;
import com.onlyoffice.docspacepipedrive.web.dto.user.UserResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
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

    @PostMapping
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        DocspaceAccount docspaceAccount = new DocspaceAccount();
        if (request.getSystem()) {
            PipedriveUser pipedriveUser = pipedriveClient.getUser();
            if (!pipedriveUser.getIsAdmin()
                    || pipedriveUser.getAccess().stream()
                    .filter(access -> access.getApp().equals("global") && access.getAdmin())
                    .toList().size() == 0
            ) {
                throw new PipedriveAccessDeniedException(currentUser.getUserId());
            }

            DocspaceAuthentication docspaceAuthentication = docspaceClient.login(
                    request.getDocspaceAccount().getUserName(),
                    request.getDocspaceAccount().getPasswordHash()
            );

            DocspaceToken docspaceToken = DocspaceToken.builder()
                    .value(docspaceAuthentication.getToken())
                    .build();

            DocspaceUser docspaceUser = docspaceClient.getUser(
                    request.getDocspaceAccount().getUserName(),
                    docspaceToken
            );

            if (!docspaceUser.getIsAdmin()) {
                throw new DocspaceAccessDeniedException(request.getDocspaceAccount().getUserName());
            }

            docspaceAccount.setUuid(docspaceUser.getId());
            docspaceAccount.setEmail(docspaceUser.getEmail());
            docspaceAccount.setPasswordHash(request.getDocspaceAccount().getPasswordHash());
            docspaceAccount.setDocspaceToken(docspaceToken);

            currentUser.setSystem(true);
            userService.put(currentUser.getClient().getId(), currentUser);

            docspaceAccountService.save(currentUser.getId(), docspaceAccount);
        } else {
            DocspaceUser docspaceUser = docspaceClient.getUser(request.getDocspaceAccount().getUserName());

            docspaceAccount.setUuid(docspaceUser.getId());
            docspaceAccount.setPasswordHash(request.getDocspaceAccount().getPasswordHash());

            docspaceAccountService.save(currentUser.getId(), docspaceAccount);
        }

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/docspace-account")
    public ResponseEntity<Void> deleteDocspaceAccount() {
        User currentUser = SecurityUtils.getCurrentUser();

        docspaceAccountService.deleteById(currentUser.getId());

        if (currentUser.getSystem()) {
            currentUser.setSystem(false);
            userService.put(currentUser.getClient().getId(), currentUser);
        }

        return ResponseEntity.noContent().build();
    }
}
