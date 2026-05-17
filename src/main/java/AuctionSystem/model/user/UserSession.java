package AuctionSystem.model.user;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String role;

    private UserSession() {} // Private constructor để ngăn tạo mới bằng lệnh new

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Getter và Setter để lưu/lấy thông tin
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Xóa session khi người dùng đăng xuất
    public void cleanUserSession() {
        username = null;
        role = null;
    }
}