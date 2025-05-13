package com.example.flashsale.controller;

import com.example.flashsale.model.User;
import com.example.flashsale.model.dto.ApiResponse;
import com.example.flashsale.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserController class.
 * These tests verify the behavior of all endpoints in the user management API,
 * including user creation, retrieval, and existence checking.
 * The tests use Mockito to mock the UserService dependency.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    /**
     * Sets up test fixtures before each test method.
     * Creates a test user with predefined ID, username, and email.
     */
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId("test123");
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
    }

    /**
     * Tests the successful creation of a new user.
     * Verifies that:
     * - The service method is called with correct parameters
     * - Response has CREATED status
     * - Response body contains the created user
     * - Success flag is true in the response
     */
    @Test
    void createUser_Success() {
        // Arrange
        when(userService.createUserIfNotExists(anyString(), anyString(), anyString()))
                .thenReturn(testUser);

        // Act
        ResponseEntity<ApiResponse<User>> response = userController.createUser(
                "test123", "testUser", "test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUser, response.getBody().getData());

        verify(userService).createUserIfNotExists("test123", "testUser", "test@example.com");
    }

    /**
     * Tests retrieving an existing user by ID.
     * Verifies that:
     * - The service method is called with correct user ID
     * - Response has OK status
     * - Response body contains the requested user
     * - Success flag is true in the response
     */
    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        // Arrange
        when(userService.getUserByUserId("test123")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<ApiResponse<User>> response = userController.getUserById("test123");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUser, response.getBody().getData());

        verify(userService).getUserByUserId("test123");
    }

    /**
     * Tests retrieving a non-existent user by ID.
     * Verifies that:
     * - The service method is called with correct user ID
     * - Response has NOT_FOUND status
     * - Response body contains appropriate error message
     * - Success flag is false in the response
     * - Data field is null in the response
     */
    @Test
    void getUserById_WhenUserDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(userService.getUserByUserId("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<User>> response = userController.getUserById("nonexistent");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        assertEquals("User not found with id: nonexistent", response.getBody().getMessage());

        verify(userService).getUserByUserId("nonexistent");
    }

    /**
     * Tests checking existence of an existing user.
     * Verifies that:
     * - The service method is called with correct user ID
     * - Response has OK status
     * - Response body contains true
     * - Success flag is true in the response
     */
    @Test
    void checkUserExists_WhenUserExists_ReturnsTrue() {
        // Arrange
        when(userService.existsByUserId("test123")).thenReturn(true);

        // Act
        ResponseEntity<ApiResponse<Boolean>> response = userController.checkUserExists("test123");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());

        verify(userService).existsByUserId("test123");
    }

    /**
     * Tests checking existence of a non-existent user.
     * Verifies that:
     * - The service method is called with correct user ID
     * - Response has OK status
     * - Response body contains false
     * - Success flag is true in the response
     */
    @Test
    void checkUserExists_WhenUserDoesNotExist_ReturnsFalse() {
        // Arrange
        when(userService.existsByUserId("nonexistent")).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<Boolean>> response = userController.checkUserExists("nonexistent");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertFalse(response.getBody().getData());

        verify(userService).existsByUserId("nonexistent");
    }
}