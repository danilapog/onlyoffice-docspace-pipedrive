package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.repository.UserRepository;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final ClientService clientService;
    private final UserRepository userRepository;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User findByUserIdAndClientId(Long userId, Long clientId) {
        return userRepository.findByUserIdAndClientId(userId, clientId)
                .orElseThrow(() -> new UserNotFoundException(userId, clientId));
    }

    @Override
    public User put(Long clientId, User user) {
        try {
            User existedUser = findByUserIdAndClientId(user.getUserId(), clientId);

            if (user.getAccessToken() != null) {
                existedUser.setAccessToken(user.getAccessToken());
            }

            if (user.getRefreshToken() != null) {
                existedUser.setRefreshToken(user.getRefreshToken());
            }

            return userRepository.save(existedUser);
        } catch (UserNotFoundException e) {
            Client client = clientService.findById(clientId);

            user.setClient(client);

            return userRepository.save(user);
        }
    }

    @Override
    public void deleteByUserIdAndClientId(Long userId, Long clientId) {
        userRepository.delete(findByUserIdAndClientId(userId, clientId));
    }
}
