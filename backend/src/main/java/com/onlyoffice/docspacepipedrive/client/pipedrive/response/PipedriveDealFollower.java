package com.onlyoffice.docspacepipedrive.client.pipedrive.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PipedriveDealFollower {
    private Long userId;
}
