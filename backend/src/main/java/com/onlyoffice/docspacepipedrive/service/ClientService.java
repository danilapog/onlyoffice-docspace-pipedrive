package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.Client;


public interface ClientService {
    Client findById(Long id);
    boolean existById(Long id);
    Client create(Client client);
    Client update(Client client);
    void unsetSystemUser(Long clientId);
    void delete(Long id);
}
