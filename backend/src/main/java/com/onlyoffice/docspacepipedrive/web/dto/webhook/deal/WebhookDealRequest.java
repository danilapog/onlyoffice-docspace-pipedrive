package com.onlyoffice.docspacepipedrive.web.dto.webhook.deal;

import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveDeal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class WebhookDealRequest {
    private Map<String, Object> meta;
    private PipedriveDeal current;
    private PipedriveDeal previous;
}
