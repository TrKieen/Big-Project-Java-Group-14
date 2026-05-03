package com.example.logincontroller.model.auction;

import com.example.logincontroller.model.Item;
import com.example.logincontroller.model.user.Bidder;
import java.util.ArrayList;
import java.util.List;

public class Auction {

    private Item item;
    private AuctionStatus status;
    private Bidder leadingBidder;
    private List<BidTransaction> transactionHistory; // Lưu lịch sử đấu giá

    public Auction(Item item) {
        this.item = item;
        this.status = AuctionStatus.OPEN;
        this.transactionHistory = new ArrayList<>();
    }

    public void startAuction() {
        status = AuctionStatus.RUNNING;
    }

    // synchronized giúp tránh lost update(Dữ liệu đặt giá của người này bị ghi đè bởi người khác.) / race condition (hai hoặc nhiều luồng cùng truy cập và thao tác trên cùng một dữ liệu)
    public synchronized void placeBid(Bidder bidder, double price) {
        if (status != AuctionStatus.RUNNING) {
            throw new RuntimeException("Phiên đấu giá chưa mở hoặc đã kết thúc");
        }

        if (price <= item.getCurrentHighestPrice()) {
            throw new RuntimeException("Giá đấu phải cao hơn giá hiện tại (" + item.getCurrentHighestPrice() + ")");
        }

        // Cập nhật mức giá mới
        item.setCurrentHighestPrice(price);
        leadingBidder = bidder;

        // Thêm vào lịch sử giao dịch
        transactionHistory.add(new BidTransaction(bidder, price));
        System.out.println("Bid thành công: " + bidder.getUsername() + " đã đặt giá $" + price);
    }

    public synchronized void finishAuction() {
        if (status == AuctionStatus.RUNNING) {
            status = AuctionStatus.FINISHED;
            System.out.println("Phiên đấu giá cho sản phẩm [" + item.getName() + "] đã kết thúc.");

            if (leadingBidder != null) {
                System.out.println("Người chiến thắng: " + leadingBidder.getUsername() + " với giá: $" + item.getCurrentHighestPrice());
            }
        }
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
    public List<BidTransaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
}