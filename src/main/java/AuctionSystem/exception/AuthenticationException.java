package AuctionSystem.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super("Xác thực thất bại. Vui lòng đăng nhập lại.");
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
