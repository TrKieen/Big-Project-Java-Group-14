package AuctionSystem.exception;

public class AuctionClosedException extends RuntimeException {
    public AuctionClosedException() {
        super("Phiên đấu giá đã kết thúc, không thể đặt giá mới.");
    }

    public AuctionClosedException(String message) {
        super(message);
    }
}
