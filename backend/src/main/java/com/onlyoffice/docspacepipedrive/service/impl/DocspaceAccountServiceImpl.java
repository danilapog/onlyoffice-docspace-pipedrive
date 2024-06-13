package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.repository.DocspaceAccountRepository;
import com.onlyoffice.docspacepipedrive.service.DocspaceAccountService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DocspaceAccountServiceImpl implements DocspaceAccountService {
    private final UserService userService;
    private final DocspaceAccountRepository docspaceAccountRepository;

    @Override
    public DocspaceAccount findByUserId(Long userId) {
        return docspaceAccountRepository.findByUserId(userId)
                .orElse(null);
    }

    @Override
    public DocspaceAccount save(Long userId, DocspaceAccount docspaceAccount) {
        User user = userService.findById(userId);

        docspaceAccount.setUser(user);

        return docspaceAccountRepository.save(docspaceAccount);
    }

    @Override
    public void deleteByUserId(Long userId) {
        DocspaceAccount docspaceAccount = findByUserId(userId);

        docspaceAccountRepository.delete(docspaceAccount);
    }
}
