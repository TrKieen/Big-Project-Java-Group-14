package AuctionSystem.model.auction;

import AuctionSystem.model.user.Bidder;
import java.time.LocalDateTime;
import java.io.Serializable;

public class Bid implements Serializable {
    private int id;
    private final double amount;
    private final LocalDateTime time;
    private final Bidder bidder;


    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public Bid(int id, Bidder bidder, double amount, LocalDateTime time) {
        this.id = id;
        this.bidder = bidder;
        this.amount = amount;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Bidder getBidder() {
        return bidder;
    }
}