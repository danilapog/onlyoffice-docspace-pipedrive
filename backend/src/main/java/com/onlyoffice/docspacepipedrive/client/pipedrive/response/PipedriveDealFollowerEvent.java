package com.onlyoffice.docspacepipedrive.client.pipedrive.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PipedriveDealFollowerEvent {
    private String timestamp;
    private Data data;

    @lombok.Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Data {
        private Integer id;
        private String action;
        private Long followerUserId;
        private String logTime;
    }
}
