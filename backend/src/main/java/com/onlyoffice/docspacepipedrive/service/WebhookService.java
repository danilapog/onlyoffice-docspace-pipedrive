package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.Webhook;

import java.util.List;
import java.util.UUID;


public interface WebhookService {
    Webhook findById(UUID id);
    List<Webhook> findAllByUserId(Long userId);
    Webhook save(Webhook webhook);
    void deleteById(UUID id);
}
