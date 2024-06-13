package com.onlyoffice.docspacepipedrive.web.mapper;

import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsSaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SettingsMapper {
    Settings settingsSaveRequestToSettings(SettingsSaveRequest settingsSaveRequest);
    SettingsResponse settingsToSettingsResponse(Settings settings);
}
