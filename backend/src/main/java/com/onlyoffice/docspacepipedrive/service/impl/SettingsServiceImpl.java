package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsNotFoundException;
import com.onlyoffice.docspacepipedrive.repository.SettingsRepository;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {
    private final ClientService clientService;
    private final SettingsRepository settingsRepository;

    @Override
    public Settings findByClientId(Long clientId) {
        return settingsRepository.findByClientId(clientId)
                .orElseThrow(() -> new SettingsNotFoundException(clientId));
    }

    @Override
    public Settings put(Long clientId, Settings settings) {
        Client client = clientService.findById(clientId);

        try {
            Settings existedSetting = findByClientId(clientId);

            if (StringUtils.hasText(settings.getUrl())) {
                existedSetting.setUrl(settings.getUrl());
            }

            if (StringUtils.hasText(settings.getUserName())) {
                existedSetting.setUserName(settings.getUserName());
            }

            if (StringUtils.hasText(settings.getPasswordHash())) {
                existedSetting.setPasswordHash(settings.getPasswordHash());
            }

            return settingsRepository.save(existedSetting);
        } catch (SettingsNotFoundException e) {
            settings.setClient(client);
            return settingsRepository.save(settings);
        }
    }
}
