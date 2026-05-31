package AuctionSystem.model.auction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {

    // ✅ volatile đảm bảo visibility đa luồng
    private static volatile AuctionManager instance;

    // ✅ CopyOnWriteArrayList: thread-safe khi đọc đồng thời (nhiều ClientHandler)
    private final List<Auction> auctions = new CopyOnWriteArrayList<>();

    private AuctionManager() {
    }

    // ✅ Double-checked locking – Singleton chuẩn thread-safe
    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // ✅ synchronized để tránh race condition khi 2 Client gửi ADD cùng lúc
    public synchronized void addAuction(Auction auction) {
        auctions.add(auction);
        System.out.println("Đã thêm phiên đấu giá cho sản phẩm: " + auction.getItem().getName());
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    // ✅ synchronized để tránh đóng phiên trùng nhau
    public synchronized void closeAuction(Auction auction) {
        auction.finishAuction();
        System.out.println("Phiên đấu giá cho sản phẩm " + auction.getItem().getName() + " đã kết thúc.");
    }
}
