package com.example.flashsale.service;

import com.example.flashsale.model.Purchase;
import com.example.flashsale.model.dto.PurchaseRequest;

import java.util.List;

public interface PurchaseService {

    boolean purchaseTickets(PurchaseRequest purchaseRequest);

    List<Purchase> getUserPurchases(String userId);

    long countUserPurchases(String userId);
}