package AuctionSystem.model.auction;

import java.util.ArrayList;
import java.util.List;

public class AuctionManager {

    private static AuctionManager instance;

    private final List<Auction> auctions;

    private AuctionManager() {
        auctions = new ArrayList<>();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addAuction(Auction auction) {
        auctions.add(auction);
        System.out.println("Đã thêm phiên đấu giá cho sản phẩm: " + auction.getItem().getDescription());
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    public void closeAuction(Auction auction) {
        auction.finishAuction();
        System.out.println("Phiên đấu giá cho sản phẩm " + auction.getItem().getDescription() + " đã kết thúc.");
    }
}
