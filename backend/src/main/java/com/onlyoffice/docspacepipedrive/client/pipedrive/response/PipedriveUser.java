package com.onlyoffice.docspacepipedrive.client.pipedrive.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PipedriveUser {
    private Long id;
    private String name;
    private Language language;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Access> access;
    private String companyDomain;

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

    public Boolean isSalesAdmin() {
        return getAccess().stream()
                .filter(access -> access.getApp().equals("sales") && access.getAdmin())
                .toList().size() > 0;
    }
}
