package com.example.flashsale.controller;

import com.example.flashsale.model.User;
import com.example.flashsale.model.dto.ApiResponse;
import com.example.flashsale.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(
            @RequestParam String userId,
            @RequestParam String username,
            @RequestParam String email) {

        log.info("Creating new user with userId: {}, username: {}", userId, username);
        User user = userService.createUserIfNotExists(userId, username, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String userId) {
        log.info("Fetching user with userId: {}", userId);
        Optional<User> userOpt = userService.getUserByUserId(userId);

        return userOpt
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found with id: " + userId)));
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkUserExists(@PathVariable String userId) {
        log.info("Checking if user exists with userId: {}", userId);
        boolean exists = userService.existsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}