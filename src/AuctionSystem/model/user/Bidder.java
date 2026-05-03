package AuctionSystem.model.user;

import AuctionSystem.model.auction.Auction;

public class Bidder extends User {

    public Bidder(String username, String password) {
        super(username, password);

    }

    public void placeBid(Auction auction, double amount) {
        auction.placeBid(this, amount);
    }

    @Override
    public String getRole() {
        return "BIDDER";
    }
}