import java.util.ArrayList;
import java.util.List;

public class Auction {
    private Item item;          // Sản phẩm được đấu giá
    private List<Bid> bids;     // Danh sách các bid
    private String status;      // Trạng thái: OPEN, RUNNING, FINISHED

    // Constructor
    public Auction(Item item) {
        this.item = item;
        this.bids = new ArrayList<>();
        this.status = "OPEN";
    }

    // Getter
    public Item getItem() {
        return item;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public String getStatus() {
        return status;
    }

    // Thêm bid mới
    public void addBid(Bid bid) {
        if (status.equals("OPEN") || status.equals("RUNNING")) {
            bids.add(bid);
            System.out.println("Bid mới: " + bid.getAmount() + " cho sản phẩm " + item.getDescription());
        } else {
            System.out.println("Phiên đấu giá đã kết thúc, không thể đặt giá.");
        }
    }

    // Đóng phiên đấu giá
    public void closeAuction() {
        status = "FINISHED";
        System.out.println("Phiên đấu giá cho sản phẩm " + item.getDescription() + " đã kết thúc.");
    }

    // Hiển thị thông tin phiên đấu giá
    public void displayAuctionInfo() {
        System.out.println("Sản phẩm: " + item.getDescription());
        System.out.println("Trạng thái: " + status);
        System.out.println("Danh sách bid:");
        for (Bid bid : bids) {
            bid.displayBidInfo();
        }
    }
}
