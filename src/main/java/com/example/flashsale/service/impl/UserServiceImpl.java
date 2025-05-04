package com.example.flashsale.service.impl;

import com.example.flashsale.model.User;
import com.example.flashsale.repository.UserRepository;
import com.example.flashsale.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUserIfNotExists(String userId, String username, String email) {
        Optional<User> existingUser = userRepository.findByUserId(userId);

        if (existingUser.isPresent()) {
            log.info("User already exists with userId: {}", userId);
            return existingUser.get();
        }

        log.info("Creating new user with userId: {}", userId);
        User newUser = User.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public Optional<User> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }
}