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

import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.events.user.DocspaceLoginUserEvent;
import com.onlyoffice.docspacepipedrive.events.user.DocspaceLogoutUserEvent;
import com.onlyoffice.docspacepipedrive.security.oauth.OAuth2PipedriveUser;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountRequest;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final DocspaceAccountService docspaceAccountService;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUser(@AuthenticationPrincipal OAuth2PipedriveUser currentUser) {
        DocspaceAccount docspaceAccount = currentUser.getUser().getDocspaceAccount();

        Map<String, Object> userResponse = currentUser.getAttributes();
        userResponse.put("isAdmin", currentUser.getAuthorities().contains(
                new SimpleGrantedAuthority("DEAL_ADMIN")
        ));
        if (Objects.nonNull(docspaceAccount)) {
            userResponse.put("docspaceAccount", new DocspaceAccountResponse(
                    docspaceAccount.getEmail(),
                    docspaceAccount.getPasswordHash()
            ));
        } else {
            userResponse.put("docspaceAccount", null);
        }


        return ResponseEntity.ok(
                userResponse
        );
    }

    @PutMapping(path = "/docspace-account")
    @Transactional
    public ResponseEntity<Void> putDocspaceAccount(@AuthenticationPrincipal OAuth2PipedriveUser currentUser,
                                                   @RequestBody DocspaceAccountRequest request) {
        DocspaceAccount docspaceAccount = DocspaceAccount.builder()
                .uuid(UUID.fromString(request.getId()))
                .email(request.getUserName())
                .passwordHash(request.getPasswordHash())
                .build();

        DocspaceAccount savedDocspaceAccount = docspaceAccountService.save(
                currentUser.getUser().getId(),
                docspaceAccount
        );

        eventPublisher.publishEvent(new DocspaceLoginUserEvent(this, savedDocspaceAccount));

        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/docspace-account")
    public ResponseEntity<Void> deleteDocspaceAccount(@AuthenticationPrincipal OAuth2PipedriveUser currentUser) {
        docspaceAccountService.deleteById(currentUser.getUser().getUserId());

        eventPublisher.publishEvent(new DocspaceLogoutUserEvent(
                this, currentUser.getUser().getDocspaceAccount()
        ));

        return ResponseEntity.noContent().build();
    }
}
