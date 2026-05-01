import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    // Singleton instance
    private static AuctionManager instance;

    // Danh sách các phiên đấu giá
    private List<Auction> auctions;

    // Constructor private
    private AuctionManager() {
        auctions = new ArrayList<>();
    }

    // Phương thức truy cập duy nhất
    public static AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // Thêm phiên đấu giá
    public void addAuction(Auction auction) {
        auctions.add(auction);
        System.out.println("Đã thêm phiên đấu giá cho sản phẩm: " + auction.getItem().getDescription());
    }

    // Lấy danh sách phiên đấu giá
    public List<Auction> getAuctions() {
        return auctions;
    }

    // Đóng phiên đấu giá
    public void closeAuction(Auction auction) {
        auction.closeAuction();
        System.out.println("Phiên đấu giá cho sản phẩm " + auction.getItem().getDescription() + " đã kết thúc.");
    }
}
