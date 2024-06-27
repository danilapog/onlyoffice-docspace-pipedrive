package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.exceptions.ClientNotFoundException;
import com.onlyoffice.docspacepipedrive.repository.ClientRepository;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    @Override
    public Client findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        return client;
    }

    public boolean existById(Long id) {
        return clientRepository.existsById(id);
    }

    @Override
    public Client create(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public Client update(Client client) {
        Client existedClient = findById(client.getId());

        if (StringUtils.hasText(client.getUrl())) {
            existedClient.setUrl(client.getUrl());
        }

        if (client.getSystemUser() != null) {
            existedClient.setSystemUser(client.getSystemUser());
        }

        return clientRepository.save(existedClient);
    }

    @Override
    public void unsetSystemUser(Long clientId) {
        Client client = findById(clientId);

        client.setSystemUser(null);

        clientRepository.save(client);
    }

    @Override
    public void delete(Long id) {
        clientRepository.deleteById(id);
    }
}
