package com.onlyoffice.docspacepipedrive.web.dto.user;

import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveUser;
import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountResponse;
import com.onlyoffice.docspacepipedrive.web.dto.settings.SettingsResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private Boolean isSystem;
    private Boolean isAdmin;
    private PipedriveUser.Language language;
    private DocspaceAccountResponse docspaceAccount;
}
