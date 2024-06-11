package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.DocspaceToken;


public interface DocspaceTokenService {
    DocspaceToken findByClientId(Long clientId);
    DocspaceToken put(Long clientId, String value);
    void deleteByClientId(Long clientId);
}
