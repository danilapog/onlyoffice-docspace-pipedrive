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

package com.onlyoffice.docspacepipedrive.events.settings;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.Webhook;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettingsEventListener {
    private final RoomService roomService;
    private final UserService userService;
    private final DocspaceAccountService docspaceAccountService;
    private final DocspaceActionManager docspaceActionManager;
    private final PipedriveActionManager pipedriveActionManager;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @EventListener
    public void listen(final SettingsUpdateEvent event) {
        Client client = SecurityUtils.getCurrentClient();

        docspaceActionManager.addDomainsToCSPSettings(Arrays.asList(client.getUrl(), frontendUrl));
        docspaceActionManager.initSharedGroup();
        pipedriveActionManager.initWebhooks();
    }

    @EventListener
    public void listen(final SettingsDeleteEvent event) {
        Client client = SecurityUtils.getCurrentClient();

        roomService.deleteAllByClientId(client.getId());

        List<User> users = userService.findAllByClientId(client.getId());

        List<Long> docspaceAccountIds = users.stream()
                .filter(user -> user.getDocspaceAccount() != null)
                .map(user -> user.getId())
                .collect(Collectors.toList());

        docspaceAccountService.deleteAllByIdInBatch(docspaceAccountIds);

        List<Webhook> webhooks = users.stream()
                .filter(user -> Objects.nonNull(user.getWebhooks()) && !user.getWebhooks().isEmpty())
                .flatMap(user -> user.getWebhooks().stream())
                .toList();

        pipedriveActionManager.deleteWebhooks(webhooks);
    }
}
