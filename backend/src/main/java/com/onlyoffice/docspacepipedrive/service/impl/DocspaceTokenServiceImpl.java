package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.DocspaceToken;
import com.onlyoffice.docspacepipedrive.repository.DocspaceTokenRepository;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.DocspaceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DocspaceTokenServiceImpl implements DocspaceTokenService {
    private final ClientService clientService;
    private final DocspaceTokenRepository docspaceTokenRepository;

    @Override
    public DocspaceToken findByClientId(Long clientId) {
        return docspaceTokenRepository.findByClientId(clientId)
                .orElse(null);
    }

    @Override
    public DocspaceToken put(Long clientId, String value) {
        Client client = clientService.findById(clientId);
        DocspaceToken docspaceToken = findByClientId(clientId);

        if (docspaceToken != null) {
            docspaceToken.setValue(value);
        } else {
            docspaceToken = DocspaceToken.builder()
                    .value(value)
                    .client(client)
                    .build();
        }

        return docspaceTokenRepository.save(docspaceToken);
    }

    public void deleteByClientId(Long clientId) {
        docspaceTokenRepository.delete(findByClientId(clientId));
    }
}
