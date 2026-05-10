package AuctionSystem.exception;
public class InvalidBidException extends RuntimeException {
    public InvalidBidException() {
        super("Giá đặt không hợp lệ. Vui lòng nhập giá cao hơn giá hiện tại.");
    }

    public InvalidBidException(String message) {
        super(message);
    }
}
