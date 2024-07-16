package com.onlyoffice.docspacepipedrive.client.pipedrive.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PipedriveDeal {
    private Long id;
    private String title;
    private Integer visibleTo;
    private Integer followersCount;
    private String updateTime;
}
