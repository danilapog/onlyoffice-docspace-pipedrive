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


@Service
@RequiredArgsConstructor
public class DocspaceAccountServiceImpl implements DocspaceAccountService {
    private final ClientService clientService;
    private final UserService userService;
    private final DocspaceAccountRepository docspaceAccountRepository;

    @Override
    public DocspaceAccount findById(Long id) {
        return docspaceAccountRepository.findById(id)
                .orElse(null);
    }

    @Override
    public DocspaceAccount save(Long id, DocspaceAccount docspaceAccount) {
        User user = userService.findById(id);

        docspaceAccount.setUser(user);
        user.setDocspaceAccount(docspaceAccount);

        return docspaceAccountRepository.save(docspaceAccount);
    }

    @Override
    public DocspaceToken saveToken(Long id, String value) {
        DocspaceAccount docspaceAccount = findById(id);

        DocspaceToken docspaceToken = DocspaceToken.builder()
                .value(value)
                .build();

        docspaceAccount.setDocspaceToken(docspaceToken);

        return docspaceAccountRepository.save(docspaceAccount).getDocspaceToken();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        User user = userService.findById(id);

        docspaceAccountRepository.deleteById(id);

        if (user.isSystemUser()) {
            clientService.unsetSystemUser(user.getClient().getId());
        }
    }
}
