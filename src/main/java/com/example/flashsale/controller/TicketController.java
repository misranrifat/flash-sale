package com.example.flashsale.controller;

import com.example.flashsale.model.Ticket;
import com.example.flashsale.model.dto.ApiResponse;
import com.example.flashsale.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Ticket>>> getAllTickets() {
        log.info("Fetching all tickets");
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Ticket>>> getAvailableTickets() {
        log.info("Fetching available tickets");
        List<Ticket> availableTickets = ticketService.getAvailableTickets();
        return ResponseEntity.ok(ApiResponse.success(availableTickets));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketStatus() {
        log.info("Fetching ticket status");
        long availableCount = ticketService.getAvailableTicketsCount();

        Map<String, Object> status = new HashMap<>();
        status.put("availableTickets", availableCount);
        status.put("soldOut", availableCount == 0);

        return ResponseEntity.ok(ApiResponse.success(status));
    }
}