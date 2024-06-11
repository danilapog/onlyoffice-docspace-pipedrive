package com.onlyoffice.docspacepipedrive.client.pipedrive.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
public class PipedriveUser {
    private Long id;
    private String name;
    private Language language;
    private List<Access> access;

    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Language {
        private String languageCode;
        private String countryCode;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Access {
        private String app;
        private Boolean admin;
        private UUID permissionSetId;
    }
}
