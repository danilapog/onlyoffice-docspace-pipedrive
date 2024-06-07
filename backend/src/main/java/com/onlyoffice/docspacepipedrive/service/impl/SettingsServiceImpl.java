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

    public Settings findByClientId(Long clientId) {
        return settingsRepository.findByClientId(clientId)
                .orElseThrow(() -> new SettingsNotFoundException(clientId));
    }

    public boolean existByClientId(Long clientId) {
        return settingsRepository.existsByClientId(clientId);
    }

    @Override
    public Settings create(Long clientId, Settings settings) {
        Client client = clientService.findById(clientId);

        settings.setClient(client);

        return settingsRepository.save(settings);
    }

    @Override
    public Settings update(Long clientId, Settings settings) {
        Settings existedSetting = findByClientId(clientId);

        if (StringUtils.hasText(settings.getToken())) {
            existedSetting.setToken(settings.getToken());
        }

        if (StringUtils.hasText(settings.getUrl())) {
            if (!existedSetting.getUrl().equals(settings.getUrl())) {
                existedSetting.setToken(null);
            }
            existedSetting.setUrl(settings.getUrl());
        }

        if (StringUtils.hasText(settings.getUserName())) {
            if (!existedSetting.getUserName().equals(settings.getUserName())) {
                existedSetting.setToken(null);
            }
            existedSetting.setUserName(settings.getUserName());
        }

        if (StringUtils.hasText(settings.getPasswordHash())) {
            if (!existedSetting.getPasswordHash().equals(settings.getPasswordHash())) {
                existedSetting.setToken(null);
            }
            existedSetting.setPasswordHash(settings.getPasswordHash());
        }

        return settingsRepository.save(existedSetting);
    }
}
