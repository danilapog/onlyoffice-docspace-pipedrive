package com.onlyoffice.docspacepipedrive.web.dto.user;

import com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount.DocspaceAccountRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserRequest {
    private Boolean system;
    private DocspaceAccountRequest docspaceAccount;
}
