package AuctionSystem.controller;

import AuctionSystem.model.user.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    @FXML private Button btnBuyer;
    @FXML private Button btnSeller;

    @FXML
    private void handleBuyerMode() {
        switchScene("/BidderDashboard.fxml", "Sàn đấu giá sản phẩm trực tuyến - Chế độ Người mua");
    }

    @FXML
    private void handleSellerMode() {
        switchScene("/SellerDashboard.fxml", "Hệ thống quản lý sản phẩm đấu giá - Chế độ Người bán");
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().setUsername(null);
        UserSession.getInstance().setRole(null);
        switchScene("/hello-view.fxml", "Hệ thống Đấu giá trực tuyến - Đăng nhập");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (root == null) {
                throw new IOException("Không tìm thấy file FXML tại đường dẫn: " + fxmlPath);
            }
            Stage stage = (Stage) btnBuyer.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Lỗi chuyển đổi màn hình: " + fxmlPath + " -> " + e.getMessage());
            e.printStackTrace(); // In toàn bộ stack trace để dễ debug nếu phát sinh lỗi khác
        }
    }
}