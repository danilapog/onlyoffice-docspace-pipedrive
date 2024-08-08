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

package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;
import com.onlyoffice.docspacepipedrive.repository.DocspaceAccountRepository;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DocspaceAccountServiceImpl implements DocspaceAccountService {
    private final ClientService clientService;
    private final UserService userService;
    private final DocspaceAccountRepository docspaceAccountRepository;

    @Override
    public DocspaceAccount findById(final Long id) {
        return docspaceAccountRepository.findById(id)
                .orElse(null);
    }

    @Override
    public DocspaceAccount save(final Long id, final DocspaceAccount docspaceAccount) {
        User user = userService.findById(id);

        docspaceAccount.setUser(user);
        user.setDocspaceAccount(docspaceAccount);

        return docspaceAccountRepository.save(docspaceAccount);
    }

    @Override
    public DocspaceToken saveToken(final Long id, final String value) {
        DocspaceAccount docspaceAccount = findById(id);

        DocspaceToken docspaceToken = DocspaceToken.builder()
                .value(value)
                .issuedAt(Instant.now())
                .build();

        docspaceAccount.setDocspaceToken(docspaceToken);

        return docspaceAccountRepository.save(docspaceAccount).getDocspaceToken();
    }

    @Override
    @Transactional
    public void deleteById(final Long id) {
        User user = userService.findById(id);

        docspaceAccountRepository.deleteById(id);

        if (user.isSystemUser()) {
            clientService.unsetSystemUser(user.getClient().getId());
        }
    }

    @Override
    public void deleteAllByIdInBatch(final List<Long> ids) {
        docspaceAccountRepository.deleteAllByIdInBatch(ids);
    }
}
