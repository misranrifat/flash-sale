package com.example.flashsale.service.impl;

import com.example.flashsale.exception.ResourceNotFoundException;
import com.example.flashsale.model.Purchase;
import com.example.flashsale.model.Ticket;
import com.example.flashsale.model.User;
import com.example.flashsale.model.dto.PurchaseRequest;
import com.example.flashsale.repository.PurchaseRepository;
import com.example.flashsale.repository.TicketRepository;
import com.example.flashsale.service.PurchaseService;
import com.example.flashsale.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final RedissonClient redissonClient;

    @Value("${flash-sale.redis.ticket-stock-key}")
    private String ticketStockKey;

    @Value("${flash-sale.redis.user-purchase-key}")
    private String userPurchaseKeyPrefix;

    @Override
    @Transactional
    public boolean purchaseTickets(PurchaseRequest purchaseRequest) {
        String userId = purchaseRequest.getUserId();
        int quantity = purchaseRequest.getQuantity();

        // Check if user exists
        User user = userService.getUserByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get distributed lock for the user to prevent concurrent purchases by the same
        // user
        String lockKey = "lock:user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Try to acquire the lock with a timeout
            boolean isLockAcquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!isLockAcquired) {
                log.warn("Failed to acquire lock for user: {}. Another purchase might be in progress.", userId);
                return false;
            }

            // Check ticket availability in Redis (fast check without hitting the database)
            RAtomicLong stockCounter = redissonClient.getAtomicLong(ticketStockKey);
            long availableCount = stockCounter.get();

            if (availableCount < quantity) {
                log.warn("Not enough tickets available. Requested: {}, Available: {}", quantity, availableCount);
                return false;
            }

            // Atomically decrement the stock
            if (stockCounter.addAndGet(-quantity) < 0) {
                // Rollback if we went below zero
                stockCounter.addAndGet(quantity);
                log.warn("Race condition detected, stock would go negative. Rolling back.");
                return false;
            }

            // Get available tickets from the database
            List<Ticket> availableTickets = ticketRepository.findBySold(false);

            if (availableTickets.size() < quantity) {
                // Rollback the Redis decrement if database doesn't match
                stockCounter.addAndGet(quantity);
                log.error("Database inconsistency detected! Redis count: {}, DB count: {}",
                        stockCounter.get() + quantity, availableTickets.size());
                return false;
            }

            // Process the purchase in the database
            for (int i = 0; i < quantity; i++) {
                Ticket ticket = availableTickets.get(i);
                ticket.setSold(true);
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);

                Purchase purchase = Purchase.builder()
                        .user(user)
                        .ticket(ticket)
                        .transactionId(UUID.randomUUID().toString())
                        .amount(ticket.getPrice())
                        .purchaseTime(LocalDateTime.now())
                        .status("COMPLETED")
                        .build();

                purchaseRepository.save(purchase);
            }

            log.info("Successfully processed purchase of {} tickets for user: {}", quantity, userId);
            return true;

        } catch (Exception e) {
            log.error("Error processing purchase for user: {}", userId, e);
            // Ensure we roll back the Redis count if an error occurs
            RAtomicLong stockCounter = redissonClient.getAtomicLong(ticketStockKey);
            stockCounter.addAndGet(quantity);
            return false;
        } finally {
            // Release the lock if we hold it
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<Purchase> getUserPurchases(String userId) {
        User user = userService.getUserByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return purchaseRepository.findByUser(user);
    }

    @Override
    public long countUserPurchases(String userId) {
        return purchaseRepository.countPurchasesByUserId(userId);
    }
}