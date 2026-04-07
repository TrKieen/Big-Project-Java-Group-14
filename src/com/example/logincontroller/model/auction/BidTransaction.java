package com.example.logincontroller.model.auction;

import com.example.logincontroller.model.user.Bidder;
import java.time.LocalDateTime;

public class BidTransaction {
    private Bidder bidder;
    private double price;
    private LocalDateTime time;

    public BidTransaction(Bidder bidder, double price) {
        this.bidder = bidder;
        this.price = price;
        this.time = LocalDateTime.now();
    }
}