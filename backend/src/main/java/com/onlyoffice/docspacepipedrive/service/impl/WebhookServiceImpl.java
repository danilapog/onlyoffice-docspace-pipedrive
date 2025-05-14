/**
 *
 * (c) Copyright Ascensio System SIA 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
    public boolean existsByClientIdAndName(final Long clientId, final String name) {
        return webhookRepository.existsByUser_Client_IdAndName(clientId, name);
    }

    @Override
    public Webhook findById(final UUID id) {
        return webhookRepository.findById(id)
                .orElseThrow(() -> new WebhookNotFoundException(id));
    }

    @Override
    public List<Webhook> findAllByUserId(final Long userId) {
        return webhookRepository.findAllByUserId(userId);
    }

    @Override
    public Webhook save(final Webhook webhook) {
        webhook.setPassword(passwordEncoder.encode(webhook.getPassword()));
        return webhookRepository.save(webhook);
    }

    @Override
    public void deleteById(final UUID id) {
        webhookRepository.deleteById(id);
    }
}
