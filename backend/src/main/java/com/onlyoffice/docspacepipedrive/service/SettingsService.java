package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.Settings;

import java.util.UUID;


public interface SettingsService {
    Settings findByClientId(Long clientId);
    Settings put(Long clientId, Settings settings);
    Settings saveSharedGroup(Long clientId, UUID groupId);
}
