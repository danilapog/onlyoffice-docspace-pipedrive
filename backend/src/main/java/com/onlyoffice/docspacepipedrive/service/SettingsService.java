package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.Settings;


public interface SettingsService {
    Settings findByClientId(Long clientId);
    Settings put(Long clientId, Settings settings);
}
