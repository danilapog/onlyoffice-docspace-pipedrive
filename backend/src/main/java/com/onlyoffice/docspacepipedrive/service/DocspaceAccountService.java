package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;


public interface DocspaceAccountService {
    DocspaceAccount save(Long userId, DocspaceAccount docspaceAccount);
    void deleteByUserId(Long id);
}
