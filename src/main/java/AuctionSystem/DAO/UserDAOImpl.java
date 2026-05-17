package AuctionSystem.DAO;

import AuctionSystem.database.JDBCUtil;
import AuctionSystem.model.user.*;
import java.sql.*;

public class UserDAOImpl implements UserDAO {

    @Override
    public User checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, username);
            st.setString(2, password);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role").toUpperCase();

                    // Khởi tạo đúng loại đối tượng con dựa trên vai trò lưu trong DB
                    return switch (role) {
                        case "SELLER" -> new Seller(username, password);
                        case "BIDDER" -> new Bidder(username, password);
                        case "ADMIN"  -> new Admin(username, password);
                        default -> null;
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registerUser(User user, String password) {
        // Chỉ chèn vào 3 cột: username, password, và role
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, user.getUsername());
            st.setString(2, password);
            st.setString(3, user.getRole().toUpperCase()); // Mặc định truyền role của đối tượng vào

            int rowsAffected = st.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi đăng ký tài khoản: " + e.getMessage());
        }
        return false;
    }
}