package com.onlyoffice.docspacepipedrive.web.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountResponse;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    @JsonProperty("is_admin")
    private Boolean isAdmin;
    private PipedriveUser.Language language;
    private List<PipedriveUser.Access> access;
    private DocspaceAccountResponse docspaceAccount;
    private SettingsResponse docspaceSettings;
}
