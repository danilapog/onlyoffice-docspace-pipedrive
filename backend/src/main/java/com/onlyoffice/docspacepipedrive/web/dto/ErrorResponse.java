package com.onlyoffice.docspacepipedrive.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;
    private Provider provider;

    public enum Provider {
        INTEGRATION_APP,
        PIPEDRIVE,
        DOCSPACE
    }
}
