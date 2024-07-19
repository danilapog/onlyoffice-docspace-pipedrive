package com.onlyoffice.docspacepipedrive.client.pipedrive.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PipedriveWebhook {
    private Long id;
    private String subscriptionUrl;
    private String eventAction;
    private String eventObject;
    private String httpAuthUser;
    private String httpAuthPassword;
}
