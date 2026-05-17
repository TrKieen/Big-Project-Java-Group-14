package AuctionSystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {
    // 1. Thay localhost bằng Host của Aiven, thay cổng thành 21322, và thêm đuôi ?ssl-mode=REQUIRED để bảo mật
    private static final String URL = "jdbc:mysql://btl-nhom14-database-linhdongthang2007-btlnhom14.c.aivencloud.com:21322/defaultdb?ssl-mode=REQUIRED";

    // 2. Thay root thành avnadmin
    private static final String USERNAME = "avnadmin";

    // 3. Bạn hãy NHỚ thay mật khẩu thật của bạn vào chuỗi bên dưới nhé (Mật khẩu lấy ở mục hình con mắt trên Aiven)
    private static final String PASSWORD = "AVNS_B5v1Gc7PmDWv3C_LgDM";

    public static Connection getConnection() {
        Connection c = null;
        try {
            // Đăng ký Driver MySQL với Java
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Thực hiện kết nối
            c = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    public static void closeConnection(Connection c) {
        try {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // HÀM MAIN ĐỂ TEST KẾT NỐI
    public static void main(String[] args) {
        Connection connection = JDBCUtil.getConnection();
        if (connection != null) {
            System.out.println("=== KẾT NỐI THÀNH CÔNG! ===");
            JDBCUtil.closeConnection(connection);
        } else {
            System.out.println("=== KẾT NỐI THẤT BẠI! Vui lòng kiểm tra lại. ===");
        }
    }
}