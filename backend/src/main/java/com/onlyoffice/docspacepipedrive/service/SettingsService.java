package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.Settings;


public interface SettingsService {
    Settings findByClientId(Long clientId);
    boolean existByClientId(Long clientId);
    Settings create(Long clientId, Settings settings);
    Settings update(Long clientId, Settings settings);
    Settings updateToken(Long clientId, String token);
}
