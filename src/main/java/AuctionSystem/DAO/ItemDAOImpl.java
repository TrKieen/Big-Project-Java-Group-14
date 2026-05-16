package AuctionSystem.DAO;

import AuctionSystem.database.JDBCUtil;
import AuctionSystem.model.Item;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl implements ItemDAO {

    @Override
    public boolean addItem(Item item, String itemType, String extraInfo) {
        String sql = "INSERT INTO items (id, name, description, starting_price, current_highest_price, start_time, end_time, item_type, extra_info) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, item.getId());
            st.setString(2, item.getName());
            st.setString(3, item.getDescription());
            st.setDouble(4, item.getStartingPrice());
            st.setDouble(5, item.getCurrentHighestPrice());
            st.setTimestamp(6, item.getStartTime() != null ? Timestamp.valueOf(item.getStartTime()) : null);
            st.setTimestamp(7, item.getEndTime() != null ? Timestamp.valueOf(item.getEndTime()) : null);
            st.setString(8, itemType);
            st.setString(9, extraInfo);

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DAO: Đã INSERT " + item.getName() + " vào MySQL Database.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateItem(Item item, String itemType, String extraInfo) {
        String sql = "UPDATE items SET name = ?, description = ?, starting_price = ?, current_highest_price = ?, "
                + "start_time = ?, end_time = ?, item_type = ?, extra_info = ? WHERE id = ?";

        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, item.getName());
            st.setString(2, item.getDescription());
            st.setDouble(3, item.getStartingPrice());
            st.setDouble(4, item.getCurrentHighestPrice());
            st.setTimestamp(5, item.getStartTime() != null ? Timestamp.valueOf(item.getStartTime()) : null);
            st.setTimestamp(6, item.getEndTime() != null ? Timestamp.valueOf(item.getEndTime()) : null);
            st.setString(7, itemType);
            st.setString(8, extraInfo);
            st.setString(9, item.getId());

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DAO: Đã UPDATE " + item.getName() + " trong MySQL Database.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteItem(String itemId) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, itemId);

            int rowsAffected = st.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DAO: Đã DELETE sản phẩm có ID: " + itemId + " khỏi MySQL.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCurrentPrice(String id, double newPrice) {
        // Câu lệnh SQL sử dụng dấu ? làm tham số giữ chỗ để tránh lỗi cú pháp và bảo mật SQL Injection
        String sql = "UPDATE items SET current_highest_price = ? WHERE id = ?";

        // Sử dụng try-with-resources để tự động đóng Connection và PreparedStatement sau khi dùng xong
        // Gọi JDBCUtil.getConnection() để lấy kết nối tới MySQL Database của dự án
        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            // Truyền dữ liệu thật vào các dấu ? theo đúng thứ tự từ trái qua phải
            st.setDouble(1, newPrice); // Dấu ? thứ nhất nhận giá tiền mới (kiểu double)
            st.setString(2, id);       // Dấu ? thứ hai nhận ID của sản phẩm (kiểu String)

            // Chạy lệnh UPDATE và trả về số dòng bị tác động trong MySQL
            int rowsAffected = st.executeUpdate();

            // Nếu có ít nhất 1 dòng được cập nhật thành công thành công thì trả về true
            if (rowsAffected > 0) {
                System.out.println("DAO: Đã cập nhật giá mới " + newPrice + " cho sản phẩm ID: " + id + " trong MySQL.");
                return true;
            }
        } catch (SQLException e) {
            // In ra nhật ký lỗi nếu kết nối Database thất bại hoặc sai tên cột/bảng
            e.printStackTrace();
        }
        return false; // Trả về false nếu không có dòng nào được cập nhật hoặc xảy ra lỗi ngoại lệ
    }

    @Override
    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection con = AuctionSystem.database.JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double startingPrice = rs.getDouble("starting_price");
                double currentHighestPrice = rs.getDouble("current_highest_price");

                Timestamp startTimestamp = rs.getTimestamp("start_time");
                LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;

                Timestamp endTimestamp = rs.getTimestamp("end_time");
                LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

                String itemType = rs.getString("item_type");
                String extraInfo = rs.getString("extra_info");

                Item item = AuctionSystem.model.ItemFactory.createItem(
                        itemType, id, name, description, startingPrice, startTime, endTime, extraInfo
                );

                if (item != null) {
                    item.setCurrentHighestPrice(currentHighestPrice);
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}