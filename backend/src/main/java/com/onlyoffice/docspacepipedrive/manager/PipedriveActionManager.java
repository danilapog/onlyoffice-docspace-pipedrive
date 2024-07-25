package com.onlyoffice.docspacepipedrive.manager;

import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.request.PipedriveWebhook;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.Webhook;
import com.onlyoffice.docspacepipedrive.security.util.RandomPasswordGenerator;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
public class PipedriveActionManager {
    private final PipedriveClient pipedriveClient;
    private final WebhookService webhookService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public void initWebhooks() {
        initWebhook("deal", "updated");
        initWebhook("user", "updated");
    }

    @Transactional
    public void removeWebhooks() {
        User user = SecurityUtils.getCurrentUser();

        List<Webhook> webhooks = webhookService.findAllByUserId(user.getId());

        for (Webhook webhook : webhooks) {
            pipedriveClient.deleteWebhook(webhook.getWebhookId());
            webhookService.deleteById(webhook.getId());
        }
    }

    private void initWebhook(String eventObject, String eventAction) {
        User user = SecurityUtils.getCurrentUser();

        Webhook webhook = Webhook.builder()
                .name(eventObject + "." + eventAction)
                .user(user)
                .password(RandomPasswordGenerator.generatePassword(32))
                .build();

        Webhook savedWebhook = webhookService.save(webhook);

        PipedriveWebhook pipedriveWebhook = PipedriveWebhook.builder()
                .subscriptionUrl(baseUrl + "/api/v1/webhook/" + eventObject)
                .eventAction(eventAction)
                .eventObject(eventObject)
                .httpAuthUser(savedWebhook.getId().toString())
                .httpAuthPassword(webhook.getPassword())
                .build();

        pipedriveWebhook = pipedriveClient.createWebhook(pipedriveWebhook);

        savedWebhook.setWebhookId(pipedriveWebhook.getId());
        webhookService.save(savedWebhook);
    }
}
