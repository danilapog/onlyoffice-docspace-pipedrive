package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.exceptions.SettingsNotFoundException;
import com.onlyoffice.docspacepipedrive.repository.SettingsRepository;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.DocspaceTokenService;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {
    private final ClientService clientService;
    private final DocspaceTokenService docspaceTokenService;
    private final SettingsRepository settingsRepository;

    @Override
    public Settings findByClientId(Long clientId) {
        return settingsRepository.findByClientId(clientId)
                .orElseThrow(() -> new SettingsNotFoundException(clientId));
    }

    @Override
    @Transactional
    public Settings put(Long clientId, Settings settings) {
        Client client = clientService.findById(clientId);

        try {
            Settings existedSetting = findByClientId(clientId);
            Boolean deleteToken = false;

            if (StringUtils.hasText(settings.getUrl())
                    && !existedSetting.getUrl().equals(settings.getUrl())) {
                existedSetting.setUrl(settings.getUrl());
                deleteToken = true;
            }

            if (StringUtils.hasText(settings.getUserName())
                    && !existedSetting.getUserName().equals(settings.getUserName())) {
                existedSetting.setUserName(settings.getUserName());
                deleteToken = true;
            }

            if (StringUtils.hasText(settings.getPasswordHash())
                    && !existedSetting.getPasswordHash().equals(settings.getPasswordHash())) {
                existedSetting.setPasswordHash(settings.getPasswordHash());
                deleteToken = true;
            }

            if (deleteToken) {
                docspaceTokenService.deleteByClientId(clientId);
            }

            return settingsRepository.save(existedSetting);
        } catch (SettingsNotFoundException e) {
            settings.setClient(client);
            return settingsRepository.save(settings);
        }
    }
}
