package AuctionSystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/btl-nhom14";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

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
            System.out.println("=== KẾT NỐI ĐẾN MYSQL THÀNH CÔNG! ===");
            JDBCUtil.closeConnection(connection);
        } else {
            System.out.println("=== KẾT NỐI THẤT BẠI! Vui lòng kiểm tra lại. ===");
        }
    }
}