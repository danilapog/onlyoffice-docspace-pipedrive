package com.onlyoffice.docspacepipedrive.exceptions;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;


public class DocspaceWebClientResponseException extends WebClientResponseException {
    public DocspaceWebClientResponseException(WebClientResponseException e) {
        super(
                e.getMessage(),
                e.getStatusCode(),
                e.getStatusText(),
                e.getHeaders(),
                e.getResponseBodyAsByteArray(),
                StandardCharsets.UTF_8,
                e.getRequest()
        );
    }
}
