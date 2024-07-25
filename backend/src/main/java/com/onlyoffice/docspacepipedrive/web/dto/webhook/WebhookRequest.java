package com.onlyoffice.docspacepipedrive.web.dto.webhook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class WebhookRequest<T> {
    private Map<String, Object> meta;
    private T current;
    private T previous;
}
