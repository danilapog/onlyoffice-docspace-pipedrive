package com.onlyoffice.docspacepipedrive.web.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class SettingsSaveRequest {
    private String url;
    private String userName;
    private String passwordHash;
}
