package com.example.flashsale.repository;

import com.example.flashsale.model.Purchase;
import com.example.flashsale.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByUser(User user);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.user.userId = :userId")
    long countPurchasesByUserId(String userId);
}