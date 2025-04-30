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

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.settings.ApiKey;
import com.onlyoffice.docspacepipedrive.events.settings.SettingsDeleteEvent;
import com.onlyoffice.docspacepipedrive.events.settings.SettingsUpdateEvent;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceApiKeyNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceUrlNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.DocspaceSettingsValidator;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.oauth.OAuth2PipedriveUser;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsRequest;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {
    private static final int API_KEY_PREFIX_LENGTH = 3;
    private static final int API_KEY_SUFFIX_LENGTH = 4;

    private final SettingsService settingsService;
    private final PipedriveActionManager pipedriveActionManager;
    private final DocspaceSettingsValidator docspaceSettingsValidator;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping
    public ResponseEntity<SettingsResponse> get(@AuthenticationPrincipal OAuth2PipedriveUser currentUser) {
        Settings settings;
        try {
            settings = settingsService.findByClientId(currentUser.getClientId());
        } catch (SettingsNotFoundException e) {
            settings = new Settings();
        }

        SettingsResponse settingsResponse = new SettingsResponse();

        try {
            settingsResponse.setUrl(settings.getUrl());
        } catch (DocspaceUrlNotFoundException e) {
            settingsResponse.setUrl("");
        }

        try {
            settingsResponse.setApiKey(formatApiKey(settings.getApiKey().getValue()));
            settingsResponse.setIsApiKeyValid(settings.getApiKey().isValid());
        } catch (DocspaceApiKeyNotFoundException e) {
            settingsResponse.setApiKey("");
            settingsResponse.setIsApiKeyValid(false);
        }

        settingsResponse.setIsWebhooksInstalled(pipedriveActionManager.isWebhooksInstalled(currentUser.getClientId()));

        return ResponseEntity.ok(settingsResponse);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('DEAL_ADMIN')")
    public ResponseEntity<SettingsResponse> save(@AuthenticationPrincipal OAuth2PipedriveUser currentUser,
                                                 @RequestBody SettingsRequest request) {
        ApiKey apiKey = ApiKey.builder()
                .value(request.getApiKey())
                .valid(true)
                .build();

        Settings settings = docspaceSettingsValidator.validate(
                Settings.builder()
                        .url(request.getUrl())
                        .apiKey(apiKey)
                        .build()
        );

        Settings savedSettings = settingsService.put(currentUser.getClientId(), settings);

        eventPublisher.publishEvent(new SettingsUpdateEvent(this, savedSettings));

        SettingsResponse settingsResponse = new SettingsResponse();

        try {
            settingsResponse.setUrl(savedSettings.getUrl());
            settingsResponse.setApiKey(formatApiKey(savedSettings.getApiKey().getValue()));
            settingsResponse.setIsApiKeyValid(savedSettings.getApiKey().isValid());
            settingsResponse.setIsWebhooksInstalled(pipedriveActionManager.isWebhooksInstalled());
        } catch (DocspaceUrlNotFoundException e) {
            settingsResponse.setUrl("");
        }

        return ResponseEntity.ok(settingsResponse);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('DEAL_ADMIN')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal OAuth2PipedriveUser currentUser) {
        settingsService.clear(currentUser.getClientId());

        eventPublisher.publishEvent(new SettingsDeleteEvent(this));

        return ResponseEntity.noContent().build();
    }

    @PostMapping("validate-api-key")
    public ResponseEntity<SettingsResponse> validateApiKey(
            @AuthenticationPrincipal(expression = "client") Client currentClient) {
        Settings currentSettings = currentClient.getSettings();

        Settings settings = docspaceSettingsValidator.validate(currentSettings);

        Settings savedSettings = settingsService.put(
                currentClient.getId(),
                settings
        );

        SettingsResponse settingsResponse = new SettingsResponse();
        settingsResponse.setUrl(savedSettings.getUrl());
        settingsResponse.setApiKey(formatApiKey(savedSettings.getApiKey().getValue()));
        settingsResponse.setIsApiKeyValid(settings.getApiKey().isValid());
        settingsResponse.setIsWebhooksInstalled(pipedriveActionManager.isWebhooksInstalled());

        return ResponseEntity.ok(settingsResponse);
    }

    private String formatApiKey(final String apiKey) {
        String prefix = apiKey.substring(0, API_KEY_PREFIX_LENGTH);
        String suffix = apiKey.substring(apiKey.length() - API_KEY_SUFFIX_LENGTH);

        return prefix + "***" + suffix;
    }
}
