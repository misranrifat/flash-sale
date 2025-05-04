package com.example.flashsale.controller;

import com.example.flashsale.model.Purchase;
import com.example.flashsale.model.dto.ApiResponse;
import com.example.flashsale.model.dto.PurchaseRequest;
import com.example.flashsale.service.PurchaseService;
import com.example.flashsale.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> purchaseTickets(
            @Valid @RequestBody PurchaseRequest request) {
        log.info("Purchase request received from user: {}, quantity: {}", request.getUserId(), request.getQuantity());

        // Check if tickets are available
        if (!ticketService.checkTicketAvailability(request.getQuantity())) {
            log.warn("Not enough tickets available for purchase. Requested: {}", request.getQuantity());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Not enough tickets available for purchase"));
        }

        // Process the purchase
        boolean purchaseSuccessful = purchaseService.purchaseTickets(request);

        if (purchaseSuccessful) {
            Map<String, Object> response = new HashMap<>();
            response.put("userId", request.getUserId());
            response.put("quantityPurchased", request.getQuantity());
            response.put("remainingTickets", ticketService.getAvailableTicketsCount());

            return ResponseEntity.ok(ApiResponse.success("Purchase successful", response));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Purchase failed. Please try again later."));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Purchase>>> getUserPurchases(@PathVariable String userId) {
        log.info("Fetching purchases for user: {}", userId);
        List<Purchase> purchases = purchaseService.getUserPurchases(userId);
        return ResponseEntity.ok(ApiResponse.success(purchases));
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserPurchaseCount(@PathVariable String userId) {
        log.info("Fetching purchase count for user: {}", userId);
        long count = purchaseService.countUserPurchases(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("purchaseCount", count);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}