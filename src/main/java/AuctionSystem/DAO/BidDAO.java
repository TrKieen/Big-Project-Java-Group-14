package AuctionSystem.DAO;

import AuctionSystem.database.JDBCUtil;
import AuctionSystem.model.auction.Bid;
import AuctionSystem.model.user.Bidder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAO {

    // 1. Hàm lưu một lượt đặt giá mới xuống database khi Client bấm nút đặt giá
    public boolean addBid(String itemId, String bidderName, double amount) {
        String sql = "INSERT INTO bids (item_id, bidder_username, bid_price) VALUES (?, ?, ?)";
        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, itemId);
            st.setString(2, bidderName);
            st.setDouble(3, amount);

            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Hàm đọc toàn bộ lịch sử đặt giá của một sản phẩm (Sắp xếp giá tăng dần để vẽ biểu đồ đường)
    public List<Bid> getBidHistory(String itemId) {
        List<Bid> history = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE item_id = ? ORDER BY bid_price ASC";

        try (Connection con = JDBCUtil.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setString(1, itemId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("bidder_username");
                    double amount = rs.getDouble("bid_price");
                    Timestamp ts = rs.getTimestamp("bid_time");
                    int id = rs.getInt("id");

                    // Khởi tạo đối tượng Bidder và Bid khớp với constructor trong file Bid.java của bạn
                    Bidder bidder = new Bidder(username, "");
                    java.time.LocalDateTime time = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();

                    Bid bid = new Bid(id, bidder, amount, time);
                    history.add(bid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}