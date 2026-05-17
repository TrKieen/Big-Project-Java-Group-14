package AuctionSystem.DAO;

import AuctionSystem.model.user.User;

public interface UserDAO {
    // Kiểm tra tài khoản khi đăng nhập, trả về đối tượng User (Seller, Bidder, Admin)
    User checkLogin(String username, String password);

    // Lưu tài khoản mới đăng ký xuống Database
    boolean registerUser(User user, String password);
}