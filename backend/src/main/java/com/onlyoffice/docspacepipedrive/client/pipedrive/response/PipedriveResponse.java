package com.onlyoffice.docspacepipedrive.client.pipedrive.response;

import lombok.Data;


@Data
public class PipedriveResponse<T> {
    private Boolean success;
    private T data;
}
