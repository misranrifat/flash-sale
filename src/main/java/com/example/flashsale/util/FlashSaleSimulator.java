package com.example.flashsale.util;

import com.example.flashsale.model.dto.ApiResponse;
import com.example.flashsale.model.dto.PurchaseRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simulator to test the flash sale system by creating a user and
 * attempting to purchase 1000 tickets using concurrent requests.
 * 
 * To run this simulator, add the profile "simulator" to your application
 * properties
 * or run with: ./gradlew bootRun --args="--spring.profiles.active=simulator"
 */
@Component
@Profile("simulator")
@RequiredArgsConstructor
@Slf4j
public class FlashSaleSimulator implements CommandLineRunner {

    private final String BASE_URL = "http://localhost:8080/api";
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Flash Sale Simulator...");

        // Generate random user ID
        String userId = UUID.randomUUID().toString();
        String username = "testuser_" + userId.substring(0, 8);
        String email = username + "@example.com";

        // Create user
        createUser(userId, username, email);

        // Check available tickets
        checkTicketStatus();

        // Attempt to purchase 1000 tickets using multiple threads
        int totalRequests = 1000;
        int threadCount = 50;

        log.info("Starting {} purchase requests with {} threads", totalRequests, threadCount);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < totalRequests; i++) {
            final int requestNumber = i + 1;
            executorService.submit(() -> {
                try {
                    purchaseTicket(userId, requestNumber);
                } catch (Exception e) {
                    log.error("Error in purchase request #{}: {}", requestNumber, e.getMessage());
                    failureCount.incrementAndGet();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.MINUTES);

        // Check final ticket status
        checkTicketStatus();

        // Display results
        log.info("Simulation completed.");
        log.info("User ID: {}", userId);
        log.info("Successful purchases: {}", successCount.get());
        log.info("Failed purchases: {}", failureCount.get());

        // Check user's actual purchases
        checkUserPurchases(userId);
    }

    private void createUser(String userId, String username, String email) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/users")
                .queryParam("userId", userId)
                .queryParam("username", username)
                .queryParam("email", email)
                .toUriString();

        ResponseEntity<ApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                });

        log.info("User created: {}", response.getBody());
    }

    private void checkTicketStatus() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                BASE_URL + "/tickets/status",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                });

        Map<String, Object> status = response.getBody().getData();
        log.info("Ticket status: Available tickets = {}, Sold Out = {}",
                status.get("availableTickets"), status.get("soldOut"));
    }

    private void purchaseTicket(String userId, int requestNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        PurchaseRequest request = PurchaseRequest.builder()
                .userId(userId)
                .quantity(1)
                .build();

        HttpEntity<PurchaseRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                    BASE_URL + "/purchases",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                    });

            ApiResponse<Map<String, Object>> body = response.getBody();
            if (body.isSuccess()) {
                log.info("Purchase request #{} successful. Remaining tickets: {}",
                        requestNumber, body.getData().get("remainingTickets"));
                successCount.incrementAndGet();
            } else {
                log.info("Purchase request #{} failed: {}", requestNumber, body.getMessage());
                failureCount.incrementAndGet();
            }
        } catch (Exception e) {
            log.warn("Purchase request #{} error: {}", requestNumber, e.getMessage());
            failureCount.incrementAndGet();
        }
    }

    private void checkUserPurchases(String userId) {
        ResponseEntity<ApiResponse<Map<String, Object>>> response = restTemplate.exchange(
                BASE_URL + "/purchases/user/" + userId + "/count",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {
                });

        Map<String, Object> purchaseData = response.getBody().getData();
        log.info("User purchase count from database: {}", purchaseData.get("purchaseCount"));
    }
}