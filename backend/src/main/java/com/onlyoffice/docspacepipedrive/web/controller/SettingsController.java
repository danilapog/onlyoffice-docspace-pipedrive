package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceUser;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsNotFoundException;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.DocspaceTokenService;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsSaveRequest;
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
    private final DocspaceClient docspaceClient;
    private final PipedriveClient pipedriveClient;
    private final DocspaceTokenService docspaceTokenService;

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
    public ResponseEntity<SettingsResponse> save(@RequestBody SettingsSaveRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        PipedriveUser pipedriveUser = pipedriveClient.getUser();

        if (!pipedriveUser.getIsAdmin()
            || pipedriveUser.getAccess().stream()
                .filter(access -> access.getApp().equals("global") && access.getAdmin())
                .toList().size() == 0
        ) {
            throw new RuntimeException("Pipedrive user is not admin"); //ToDo
        }

        Settings settings = settingsMapper.settingsSaveRequestToSettings(request);
        settings.setClient(currentUser.getClient());

        DocspaceUser docspaceUser = docspaceClient.getUser(settings.getUserName(), settings);

        if (!docspaceUser.getIsAdmin()) {
            docspaceTokenService.deleteByClientId(currentUser.getClient().getId());
            throw new RuntimeException("Docspace user is not admin"); //ToDo
        }

        settings = settingsService.put(
                currentUser.getClient().getId(),
                settingsMapper.settingsSaveRequestToSettings(request)
        );

        return ResponseEntity.ok(
                settingsMapper.settingsToSettingsResponse(settings)
        );
    }
}
