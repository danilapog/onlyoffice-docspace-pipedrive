package com.onlyoffice.docspacepipedrive.client.docspace.response;

import lombok.Data;


@Data
public class DocspaceResponse<T> {
    private Integer status;
    private Integer statusCode;
    private T response;
}
