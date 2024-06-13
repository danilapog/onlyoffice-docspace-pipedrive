package com.onlyoffice.docspacepipedrive.web.dto.docspaceaccount;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class DocspaceAccountSaveRequest {
    private String userName;
    private String passwordHash;
}
