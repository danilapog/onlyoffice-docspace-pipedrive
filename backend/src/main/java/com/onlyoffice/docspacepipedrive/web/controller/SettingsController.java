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
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsNotFoundException;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsRequest;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import com.onlyoffice.docspacepipedrive.web.mapper.SettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {
    private final SettingsService settingsService;
    private final SettingsMapper settingsMapper;
    private final PipedriveClient pipedriveClient;

    @GetMapping
    public ResponseEntity<SettingsResponse> get() {
        Client currentClient = SecurityUtils.getCurrentClient();

        Settings settings;
        try {
            settings = settingsService.findByClientId(currentClient.getId());
        } catch (SettingsNotFoundException e) {
            settings = new Settings();
        }

        return ResponseEntity.ok(
                settingsMapper.settingsToSettingsResponse(
                        settings,
                        currentClient.getSystemUser() != null
                )
        );
    }

    @PostMapping
    public ResponseEntity<SettingsResponse> save(@RequestBody SettingsRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        PipedriveUser pipedriveUser = pipedriveClient.getUser();

        if (!pipedriveUser.isSalesAdmin()) {
            throw new PipedriveAccessDeniedException(currentUser.getUserId());
        }

        Settings savedSettings = settingsService.put(
                currentUser.getClient().getId(),
                settingsMapper.settingsRequestToSettings(request)
        );

        return ResponseEntity.ok(
                settingsMapper.settingsToSettingsResponse(
                        savedSettings,
                        currentUser.getClient().getSystemUser() != null
                )
        );
    }
}
