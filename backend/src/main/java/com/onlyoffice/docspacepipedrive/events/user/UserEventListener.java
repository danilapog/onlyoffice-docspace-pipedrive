/**
 *
 * (c) Copyright Ascensio System SIA 2024
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

package com.onlyoffice.docspacepipedrive.events.user;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
    private final DocspaceActionManager docspaceActionManager;
    private final PipedriveActionManager pipedriveActionManager;

    @EventListener
    public void listen(final DocspaceLoginUserEvent event) {
        User currentUser = SecurityUtils.getCurrentUser();
        Client currentClient = SecurityUtils.getCurrentClient();

        if (currentUser.isSystemUser()) {
            docspaceActionManager.initSharedGroup();
            pipedriveActionManager.initWebhooks();
        } else {
            SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
                public Void doWork() {
                    docspaceActionManager.inviteDocspaceAccountToSharedGroup(event.getDocspaceAccount().getUuid());
                    return null;
                }
            }, currentClient.getSystemUser());
        }
    }

    @EventListener
    public void listen(final DocspaceLogoutUserEvent event) {
        User currentUser = SecurityUtils.getCurrentUser();
        Client currentClient = SecurityUtils.getCurrentClient();

        if (currentUser.isSystemUser()) {
            try {
                docspaceActionManager.removeDocspaceAccountFromSharedGroup(event.getDocspaceAccount().getUuid());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

            try {
                pipedriveActionManager.removeWebhooks();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            try {
                SecurityUtils.runAs(new SecurityUtils.RunAsWork<Void>() {
                    public Void doWork() {
                        docspaceActionManager.removeDocspaceAccountFromSharedGroup(event.getDocspaceAccount().getUuid());
                        return null;
                    }
                }, currentClient.getSystemUser());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }
}
