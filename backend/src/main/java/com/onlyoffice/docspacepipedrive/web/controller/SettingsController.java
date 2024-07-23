package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
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
        User currentUser = SecurityUtils.getCurrentUser();

        Settings settings;
        try {
            settings = settingsService.findByClientId(currentUser.getClient().getId());
        } catch (SettingsNotFoundException e) {
            settings = new Settings();
        }

        return ResponseEntity.ok(
                settingsMapper.settingsToSettingsResponse(
                        settings
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
                settingsMapper.settingsToSettingsResponse(savedSettings)
        );
    }
}
