package com.onlyoffice.docspacepipedrive.client.pipedrive.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PipedriveResponse<T> {
    private Boolean success;
    private T data;
    private PipedriveResponseAdditionalData additionalData;

    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class PipedriveResponseAdditionalData {
        private PipedriveResponsePagination pagination;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class PipedriveResponsePagination {
        private Integer start;
        private Integer limit;
        private Boolean moreItemsInCollection;
        private Integer nextStart;
    }
}
