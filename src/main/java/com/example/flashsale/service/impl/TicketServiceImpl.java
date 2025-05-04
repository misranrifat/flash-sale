package com.example.flashsale.service.impl;

import com.example.flashsale.model.Ticket;
import com.example.flashsale.repository.TicketRepository;
import com.example.flashsale.service.TicketService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final RedissonClient redissonClient;

    @Value("${flash-sale.total-tickets}")
    private int totalTickets;

    @Value("${flash-sale.redis.ticket-stock-key}")
    private String ticketStockKey;

    @PostConstruct
    public void init() {
        initializeTickets(totalTickets);
    }

    @Override
    @Transactional
    public void initializeTickets(int totalTickets) {
        // Check if tickets are already initialized
        if (ticketRepository.count() > 0) {
            log.info("Tickets already initialized. Skipping initialization.");

            // Update Redis stock count based on the database state
            long availableTickets = ticketRepository.countAvailableTickets();
            RAtomicLong stock = redissonClient.getAtomicLong(ticketStockKey);
            stock.set(availableTickets);
            log.info("Updated Redis stock count to: {}", availableTickets);

            return;
        }

        log.info("Initializing {} tickets", totalTickets);

        // Create tickets in the database
        for (int i = 0; i < totalTickets; i++) {
            Ticket ticket = Ticket.builder()
                    .ticketNumber(UUID.randomUUID().toString())
                    .price(BigDecimal.valueOf(99.99))
                    .reserved(false)
                    .sold(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            ticketRepository.save(ticket);
        }

        // Initialize Redis stock
        RAtomicLong stock = redissonClient.getAtomicLong(ticketStockKey);
        stock.set(totalTickets);

        log.info("Successfully initialized {} tickets and Redis stock", totalTickets);
    }

    @Override
    public long getAvailableTicketsCount() {
        RAtomicLong stock = redissonClient.getAtomicLong(ticketStockKey);
        return stock.get();
    }

    @Override
    public boolean checkTicketAvailability(int quantity) {
        RAtomicLong stock = redissonClient.getAtomicLong(ticketStockKey);
        return stock.get() >= quantity;
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public List<Ticket> getAvailableTickets() {
        return ticketRepository.findBySold(false);
    }
}