package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountSaveRequest;
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
            docspaceUser = docspaceClient.getUser(currentUser.getDocspaceAccount().getId());
        } catch (Exception e) {
            //ToDo
        }

        return ResponseEntity.ok(
                userMapper.userToUserResponse(currentUser, pipedriveUser, docspaceUser)
        );
    }

    @PostMapping("/docspace-account")
    public ResponseEntity<UserResponse> postDocspaceAccount(@RequestBody DocspaceAccountSaveRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        DocspaceUser docspaceUser = docspaceClient.getUser(request.getUserName());

        DocspaceAccount docspaceAccount = new DocspaceAccount();
        docspaceAccount.setId(docspaceUser.getId());
        docspaceAccount.setPasswordHash(request.getPasswordHash());

        docspaceAccountService.save(currentUser.getId(), docspaceAccount);

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/docspace-account")
    public ResponseEntity<Void> postDocspaceAccount() {
        User currentUser = SecurityUtils.getCurrentUser();

        docspaceAccountService.deleteByUserId(currentUser.getId());

        return ResponseEntity.noContent().build();
    }
}
