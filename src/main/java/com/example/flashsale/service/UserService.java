package com.example.flashsale.service;

import com.example.flashsale.model.User;

import java.util.Optional;

public interface UserService {

    User createUserIfNotExists(String userId, String username, String email);

    Optional<User> getUserByUserId(String userId);

    boolean existsByUserId(String userId);
}