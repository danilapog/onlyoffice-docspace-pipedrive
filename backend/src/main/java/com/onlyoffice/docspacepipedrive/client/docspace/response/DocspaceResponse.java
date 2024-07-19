package com.onlyoffice.docspacepipedrive.client.docspace.response;

import lombok.Data;


@Data
public class DocspaceResponse<T> {
    private Integer status;
    private Integer statusCode;
    private Integer count;
    private Integer total;
    private T response;
}
