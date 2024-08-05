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

import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import com.onlyoffice.docspacepipedrive.web.aop.Mode;
import com.onlyoffice.docspacepipedrive.web.aop.pipedrive.ExecutePipedriveAction;
import com.onlyoffice.docspacepipedrive.web.aop.pipedrive.PipedriveAction;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsRequest;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.SettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {
    private final SettingsService settingsService;
    private final ClientService clientService;
    private final RoomService roomService;
    private final UserService userService;
    private final DocspaceAccountService docspaceAccountService;
    private final SettingsMapper settingsMapper;
    private final PipedriveClient pipedriveClient;
    private final PipedriveActionManager pipedriveActionManager;

    @GetMapping
    public ResponseEntity<SettingsResponse> get(@AuthenticationPrincipal(expression = "client") Client currentClient) {
        Settings settings;
        try {
            settings = settingsService.findByClientId(currentClient.getId());
        } catch (SettingsNotFoundException e) {
            settings = new Settings();
        }

        return ResponseEntity.ok(
                settingsMapper.settingsToSettingsResponse(
                        settings,
                        currentClient.existSystemUser()
                )
        );
    }

    @PostMapping
    public ResponseEntity<SettingsResponse> save(@AuthenticationPrincipal User currentUser,
                                                 @AuthenticationPrincipal(expression = "client") Client currentClient,
                                                 @RequestBody SettingsRequest request) {
        PipedriveUser pipedriveUser = pipedriveClient.getUser();

        if (!pipedriveUser.isSalesAdmin()) {
            throw new PipedriveAccessDeniedException(currentUser.getUserId());
        }

        Settings savedSettings = settingsService.put(
                currentClient.getId(),
                settingsMapper.settingsRequestToSettings(request)
        );

        return ResponseEntity.ok(
                settingsMapper.settingsToSettingsResponse(
                        savedSettings,
                        currentClient.existSystemUser()
                )
        );
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User currentUser,
                                       @AuthenticationPrincipal(expression = "client") Client currentClient) {
        PipedriveUser pipedriveUser = pipedriveClient.getUser();

        if (!pipedriveUser.isSalesAdmin()) {
            throw new PipedriveAccessDeniedException(currentUser.getUserId());
        }

        settingsService.deleteById(currentClient.getSettings().getId());
        roomService.deleteAllByClientId(currentClient.getId());

        List<User> users = userService.findAllByClientId(currentClient.getId());

        List<Long> docspaceAccountIds = users.stream()
                .filter(user -> user.getDocspaceAccount() != null)
                .map(user -> user.getId())
                .collect(Collectors.toList());

        docspaceAccountService.deleteAllByIdInBatch(docspaceAccountIds);

        if (currentClient.existSystemUser()) {
            SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
                public Void doWork() {
                    try {
                        pipedriveActionManager.removeWebhooks();
                    } catch (Exception e) {
                        log.warn(
                                MessageFormat.format(
                                        "An attempt execute action REMOVE_WEBHOOKS failed with the error: {1}",
                                        e.getMessage()
                                )
                        );
                    }

                    return null;
                }
            }, currentClient.getSystemUser());

            clientService.unsetSystemUser(currentClient.getId());
        }

        return ResponseEntity.noContent().build();
    }
}
