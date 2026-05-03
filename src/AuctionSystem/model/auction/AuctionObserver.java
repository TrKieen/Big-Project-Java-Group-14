package AuctionSystem.model.auction;

public interface AuctionObserver {
    void onAuctionUpdated(Auction auction);
}