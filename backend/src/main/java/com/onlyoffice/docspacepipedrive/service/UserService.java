package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.User;


public interface UserService {
    User findById(Long id);
    User create(Long clientId, User user);
    User update(User user);
    void delete(Long id);
}
