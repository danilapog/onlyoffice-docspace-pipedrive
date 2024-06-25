package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;


public interface DocspaceAccountService {
    DocspaceAccount findById(Long id);
    DocspaceAccount save(Long id, DocspaceAccount docspaceAccount);
    DocspaceToken saveToken(Long id, String value);
    void deleteById(Long id);
}
