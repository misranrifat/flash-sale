package com.example.flashsale.repository;

import com.example.flashsale.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findBySold(boolean sold);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.sold = true")
    long countSoldTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.sold = false")
    long countAvailableTickets();
}