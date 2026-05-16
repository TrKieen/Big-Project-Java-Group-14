package AuctionSystem.model.user;

import AuctionSystem.model.Item;
import AuctionSystem.model.auction.Auction;
import AuctionSystem.model.auction.AuctionManager;

public class Seller extends User {

    public Seller(String username, String password) {
        super(username, password);
    }

    public void createAuction(Item item,int durationMinutes) {
        Auction newAuction = new Auction(item,durationMinutes);
        AuctionManager.getInstance().addAuction(newAuction);
    }

    @Override
    public String getRole() {
        return "SELLER";
    }
}