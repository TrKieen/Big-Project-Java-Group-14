package AuctionSystem.controller;

import AuctionSystem.DAO.UserDAO;
import AuctionSystem.DAO.UserDAOImpl;
import AuctionSystem.model.user.Bidder; // Hoặc User/Member tùy thuộc vào model mặc định của bạn
import AuctionSystem.model.user.User;
import AuctionSystem.model.user.UserSession;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloController {
    private static final Logger LOGGER = Logger.getLogger(HelloController.class.getName());

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML private TextField txtUsername, txtRegUsername;
    @FXML private PasswordField txtPassword, txtRegPassword;
    @FXML private Label lblMessage, overlayTitle, overlayText;
    @FXML private AnchorPane overlayContainer;
    @FXML private VBox overlayContent;
    @FXML private Button switchBtn;

    // =======================================================
    // ĐÃ XÓA: rbBidder và rbSeller tại đây
    // =======================================================

    private boolean isSignUpMode = false;

    @FXML
    protected void handleSwitch() {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.7), overlayContainer);
        slide.setInterpolator(Interpolator.EASE_BOTH);

        if (!isSignUpMode) {
            slide.setToX(-350);
            slide.setOnFinished(e -> {
                overlayContent.setStyle("-fx-background-color: linear-gradient(to bottom right, #5e43f3, #4a32cc); -fx-background-radius: 30 0 0 30;");
                overlayTitle.setText("Welcome Back!");
                overlayText.setText("Đăng nhập để bắt đầu.");
                switchBtn.setText("SIGN IN");
            });
            isSignUpMode = true;
        } else {
            slide.setToX(0);
            slide.setOnFinished(e -> {
                overlayContent.setStyle("-fx-background-color: linear-gradient(to bottom right, #4a32cc, #5e43f3); -fx-background-radius: 0 30 30 0;");
                overlayTitle.setText("Hello, Friend!");
                overlayText.setText("Nếu chưa có tài khoản thì hãy đăng ký.");
                switchBtn.setText("SIGN UP");
            });
            isSignUpMode = false;
        }
        slide.play();
    }

    @FXML
    protected void onLoginButtonClick() {
        String u = txtUsername.getText().trim();
        String p = txtPassword.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            lblMessage.setText("Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        User user = userDAO.checkLogin(u, p);

        if (user != null) {
            UserSession.getInstance().setUsername(user.getUsername());
            UserSession.getInstance().setRole(user.getRole());

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                openAdminDashboard();
            } else {
                loadDashboard("/resources/RoleSelection.fxml", "Hệ thống Đấu giá UET - Lựa chọn chế độ");
            }
        } else {
            lblMessage.setText("Tài khoản hoặc mật khẩu không chính xác!");
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút Đăng ký (ĐÃ XÓA CHỌN VAI TRÒ)
     */
    @FXML
    protected void onSignUpButtonClick() {
        String u = txtRegUsername.getText().trim();
        String p = txtRegPassword.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            lblMessage.setText("Vui lòng điền đầy đủ thông tin đăng ký!");
            return;
        }

        // =======================================================
        // CẬP NHẬT: Khởi tạo mặc định một lớp User chung khi đăng ký
        // (Ví dụ dưới đây mặc định tạo một Bidder, hoặc bạn đổi thành class User phù hợp)
        // =======================================================
        User newUser = new Bidder(u, p);

        boolean isSuccess = userDAO.registerUser(newUser, p);

        if (isSuccess) {
            lblMessage.setText("Đăng ký thành công! Mời bạn đăng nhập.");
            txtUsername.setText(u);
            txtPassword.clear();
            handleSwitch();
        } else {
            lblMessage.setText("Đăng ký thất bại! Tên tài khoản đã tồn tại.");
        }
    }

    private void openSellerDashboard() { loadDashboard("/resources/SellerDashboard.fxml", "Seller Dashboard"); }
    private void openBidderDashboard() { loadDashboard("/resources/BidderDashboard.fxml", "Bidder Dashboard"); }
    private void openAdminDashboard() { loadDashboard("/resources/AdminDashboard.fxml", "Admin Dashboard"); }

    private void loadDashboard(String fxmlPath, String title) {
        try {
            URL location = getClass().getResource(fxmlPath);
            if (location == null) {
                location = getClass().getResource(fxmlPath.replace("/resources", ""));
            }

            if (location == null) {
                lblMessage.setText("Không tìm thấy file FXML!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            if (stage.isMaximized()) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            } else {
                stage.setMaximized(true);
            }
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi mở Dashboard: " + fxmlPath, e);
            lblMessage.setText("Lỗi hệ thống không thể tải trang!");
        }
    }
}