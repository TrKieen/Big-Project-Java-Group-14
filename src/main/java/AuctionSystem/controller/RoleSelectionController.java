package AuctionSystem.controller;

import AuctionSystem.model.user.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class RoleSelectionController {

    @FXML private Button btnBuyer;
    @FXML private Button btnSeller;

    @FXML
    private void handleBuyerMode(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BidderDashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại ra để xử lý
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 1. Gán Scene mới (Bỏ qua thông số kích thước cứng)
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Sàn đấu giá - Bidder Dashboard");

            // 2. MẸO QUAN TRỌNG: Ép buộc cửa sổ phải bung hết kích thước màn hình đối với giao diện mới
            if (currentStage.isMaximized()) {
                currentStage.setMaximized(false);
                currentStage.setMaximized(true);
            } else {
                currentStage.setMaximized(true);
            }

            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSellerMode(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SellerDashboard.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại ra để xử lý
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 1. Gán Scene mới (Bỏ qua thông số kích thước cứng)
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Sàn đấu giá - Bidder Dashboard");

            // 2. MẸO QUAN TRỌNG: Ép buộc cửa sổ phải bung hết kích thước màn hình đối với giao diện mới
            if (currentStage.isMaximized()) {
                currentStage.setMaximized(false);
                currentStage.setMaximized(true);
            } else {
                currentStage.setMaximized(true);
            }

            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 1. Gán Scene đăng nhập mới (Bỏ các thông số kích thước cứng)
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Đăng nhập hệ thống");

            // 2. Ép cửa sổ làm mới lại kích thước toàn màn hình
            if (currentStage.isMaximized()) {
                currentStage.setMaximized(false);
                currentStage.setMaximized(true);
            } else {
                currentStage.setMaximized(true);
            }

            currentStage.centerOnScreen();
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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