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

    public Auction(Item item) {
        this.item = item;
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new ArrayList<>();

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
}
