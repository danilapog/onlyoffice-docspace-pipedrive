package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.User;


public interface UserService {
    User findById(Long id);
    User findByUserIdAndClientId(Long userId, Long clientId);
    User put(Long clientId, User user);
    void deleteByUserIdAndClientId(Long userId, Long clientId);
}
