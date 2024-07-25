package com.onlyoffice.docspacepipedrive.web.mapper;

import com.onlyoffice.docspacepipedrive.entity.Settings;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsRequest;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SettingsMapper {
    Settings settingsRequestToSettings(SettingsRequest settingsRequest);
    @Mappings({
            @Mapping(target = "existSystemUser", source = "existSystemUser"),
    })
    SettingsResponse settingsToSettingsResponse(Settings settings, Boolean existSystemUser);
}
