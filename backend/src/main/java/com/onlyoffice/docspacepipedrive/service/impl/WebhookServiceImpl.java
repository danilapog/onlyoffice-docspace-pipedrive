package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.Webhook;
import com.onlyoffice.docspacepipedrive.exceptions.WebhookNotFoundException;
import com.onlyoffice.docspacepipedrive.repository.WebhookRepository;
import com.onlyoffice.docspacepipedrive.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {
    private final WebhookRepository webhookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Webhook findById(UUID id) {
        return webhookRepository.findById(id).
                orElseThrow(() -> new WebhookNotFoundException(id));
    }

    @Override
    public List<Webhook> findAllByUserId(Long userId) {
        return webhookRepository.findAllByUserId(userId);
    }

    @Override
    public Webhook save(Webhook webhook) {
        webhook.setPassword(passwordEncoder.encode(webhook.getPassword()));
        return webhookRepository.save(webhook);
    }

    @Override
    public void deleteById(UUID id) {
        webhookRepository.deleteById(id);
    }
}
