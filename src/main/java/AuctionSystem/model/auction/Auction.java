package AuctionSystem.model.auction;

import AuctionSystem.exception.AuctionClosedException;
import AuctionSystem.exception.InvalidBidException;
import AuctionSystem.model.Item;
import AuctionSystem.model.user.Bidder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Auction implements Serializable {
    private final Item item;
    private AuctionStatus status;
    private final List<Bid> bidHistory;
    private transient List<AuctionObserver> observers;
    private boolean closed;
    private long endTime;

    public Auction(Item item) {
        this.item = item;
        this.bidHistory = new ArrayList<>();

        // Tự động cập nhật trạng thái dựa trên thời gian thực tế của hệ thống
        updateStatusBasedOnTime();
    }
    public boolean isClosed() {
        return closed;
    }
    public void closeAuction() {
        this.closed = true;
    }

    public void addObserver(AuctionObserver observer) {
        if (observers == null) observers = new ArrayList<>();
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(AuctionObserver observer) {
        if (observers != null) observers.remove(observer);
    }

    private void notifyObservers() {
        if (observers != null) {
            for (AuctionObserver observer : observers) {
                observer.onAuctionUpdated(this);
            }
        }
    }

    public void startAuction() {
        status = AuctionStatus.RUNNING;
        notifyObservers();
    }

    public synchronized void placeBid(Bidder bidder, double price) {
        if (status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc hoặc chưa bắt đầu.");
        }
        if (price <= item.getCurrentHighestPrice()) {
            throw new InvalidBidException("Giá đặt phải cao hơn mức giá cao nhất hiện tại: " + item.getCurrentHighestPrice());
        }


        item.setCurrentHighestPrice(price);

        Bid newBid = new Bid(bidder, price);
        bidHistory.add(newBid);

        notifyObservers();
    }

    public void finishAuction() {
        status = AuctionStatus.FINISHED;
        notifyObservers();
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public Item getItem() {
        return item;
    }

    public List<Bid> getBidHistory() {
        return bidHistory;
    }

    public Bidder getLeadingBidder() {
        if (bidHistory.isEmpty()) {
            return null;
        }
        // Người đặt giá cao nhất là người ở cuối danh sách lịch sử
        return bidHistory.get(bidHistory.size() - 1).getBidder();
    }
    public String getTimeRemainingFormatted() {
        // Luôn cập nhật lại trạng thái trước khi tính thời gian còn lại
        updateStatusBasedOnTime();

        if (status == AuctionStatus.FINISHED) {
            return "00:00:00";
        }

        if (status == AuctionStatus.OPEN) {
            return "Chưa bắt đầu"; // Hoặc bạn có thể tính thời gian đếm ngược tới lúc BẮT ĐẦU
        }

        // Thời gian còn lại = Thời gian kết thúc của sản phẩm - Thời gian hiện tại
        long end = item.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long diff = end - System.currentTimeMillis();
        if (diff <= 0) {
            return "00:00:00";
        }

        long hh = (diff / (1000 * 60 * 60));
        long mm = (diff / (1000 * 60)) % 60;
        long ss = (diff / 1000) % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }
    public void updateStatusBasedOnTime() {
        if (item.getStartTime() == null || item.getEndTime() == null) {
            return; // Tránh lỗi NullPointerException nếu dữ liệu trống
        }

        long now = System.currentTimeMillis();

        // ĐỔI ĐÚNG CÁCH: Chuyển LocalDateTime sang mili-giây (long) theo múi giờ hệ thống
        long start = item.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = item.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (now < start) {
            this.status = AuctionStatus.OPEN;       // Chưa đến giờ đấu giá
            this.closed = false;
        } else if (now >= start && now <= end) {
            this.status = AuctionStatus.RUNNING;    // Đang trong thời gian đấu giá
            this.closed = false;
        } else {
            this.status = AuctionStatus.FINISHED;   // Đã quá giờ kết thúc
            this.closed = true;
        }
    }
}
