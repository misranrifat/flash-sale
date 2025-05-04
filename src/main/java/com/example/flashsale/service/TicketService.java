package com.example.flashsale.service;

import com.example.flashsale.model.Ticket;

import java.util.List;

public interface TicketService {

    void initializeTickets(int totalTickets);

    long getAvailableTicketsCount();

    boolean checkTicketAvailability(int quantity);

    List<Ticket> getAllTickets();

    List<Ticket> getAvailableTickets();
}