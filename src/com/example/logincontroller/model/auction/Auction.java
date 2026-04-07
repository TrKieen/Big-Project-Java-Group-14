package com.example.logincontroller.model.auction;

import com.example.logincontroller.model.Item;
import com.example.logincontroller.model.user.Bidder;

public class Auction {

    private Item item;
    private AuctionStatus status;
    private Bidder leadingBidder;

    public Auction(Item item) {
        this.item = item;
        this.status = AuctionStatus.OPEN;
    }

    public void startAuction() {
        status = AuctionStatus.RUNNING;
    }

    public synchronized void placeBid(Bidder bidder, double price) {
        if (status != AuctionStatus.RUNNING) {
            throw new RuntimeException("Phiên đấu giá chưa mở hoặc đã kết thúc");
        }

        if (price <= item.getCurrentHighestPrice()) {
            throw new RuntimeException("Giá đấu phải cao hơn giá hiện tại");
        }

        item.setCurrentHighestPrice(price);
        leadingBidder = bidder;
    }

    public void finishAuction() {
        status = AuctionStatus.FINISHED;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public Bidder getLeadingBidder() {
        return leadingBidder;
    }

    public Item getItem() {
        return item;
    }
}