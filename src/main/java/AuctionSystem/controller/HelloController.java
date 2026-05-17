package AuctionSystem.controller;

import AuctionSystem.DAO.UserDAO;
import AuctionSystem.DAO.UserDAOImpl;
import AuctionSystem.model.user.Bidder;
import AuctionSystem.model.user.Seller;
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
import javafx.scene.control.RadioButton; // Đã giữ lại import của bạn
import javafx.scene.control.ToggleGroup;  // Đã giữ lại import của bạn
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloController {
    private static final Logger LOGGER = Logger.getLogger(HelloController.class.getName());

    // Khai báo đối tượng UserDAO để kết nối Database đám mây Aiven
    private final UserDAO userDAO = new UserDAOImpl();

    // Các thành phần UI được liên kết từ file FXML qua ID (@FXML)
    @FXML private TextField txtUsername, txtRegUsername;
    @FXML private PasswordField txtPassword, txtRegPassword;
    @FXML private Label lblMessage, overlayTitle, overlayText;
    @FXML private AnchorPane overlayContainer; // Khối chứa hiệu ứng trượt
    @FXML private VBox overlayContent; // Nội dung bên trong khối trượt
    @FXML private Button switchBtn;

    // =======================================================
    // THÊM: Khai báo các nút vai trò tương ứng với fx:id trong FXML
    // =======================================================
    @FXML private RadioButton rbBidder;
    @FXML private RadioButton rbSeller;

    // Biến trạng thái để kiểm tra đang ở chế độ Đăng ký hay Đăng nhập
    private boolean isSignUpMode = false;

    /**
     * Xử lý hiệu ứng trượt (Animation) khi nhấn nút chuyển đổi giữa Đăng nhập và Đăng ký
     */
    @FXML
    protected void handleSwitch() {
        // Tạo hiệu ứng di chuyển (Translate) trong 0.7 giây
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.7), overlayContainer);
        slide.setInterpolator(Interpolator.EASE_BOTH); // Hiệu ứng mượt ở hai đầu

        if (!isSignUpMode) {
            // Chuyển sang chế độ Đăng ký (trượt sang trái)
            slide.setToX(-350);
            slide.setOnFinished(e -> {
                // Thay đổi màu sắc và nội dung sau khi trượt xong
                overlayContent.setStyle("-fx-background-color: linear-gradient(to bottom right, #5e43f3, #4a32cc); -fx-background-radius: 30 0 0 30;");
                overlayTitle.setText("Welcome Back!");
                overlayText.setText("Đăng nhập để bắt đầu.");
                switchBtn.setText("SIGN IN");
            });
            isSignUpMode = true;
        } else {
            // Chuyển về chế độ Đăng nhập (trượt về vị trí cũ)
            slide.setToX(0);
            slide.setOnFinished(e -> {
                overlayContent.setStyle("-fx-background-color: linear-gradient(to bottom right, #4a32cc, #5e43f3); -fx-background-radius: 0 30 30 0;");
                overlayTitle.setText("Hello, Friend!");
                overlayText.setText("Nếu chưa có tài khoản thì hãy đăng ký.");
                switchBtn.setText("SIGN UP");
            });
            isSignUpMode = false;
        }
        slide.play(); // Bắt đầu chạy animation
    }

    //Xử lý sự kiện khi nhấn nút Đăng nhập
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
                // TÀI KHOẢN ĐA NHIỆM: Chuyển tới màn hình trung gian chọn Chế độ làm việc
                loadDashboard("/resources/RoleSelection.fxml", "Hệ thống Đấu giá UET - Lựa chọn chế độ");
            }
        } else {
            lblMessage.setText("Tài khoản hoặc mật khẩu không chính xác!");
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút Đăng ký (ĐÃ ĐƯỢC FIX THEO VAI TRÒ)
     */
    @FXML
    protected void onSignUpButtonClick() {
        String u = txtRegUsername.getText().trim();
        String p = txtRegPassword.getText().trim();

        // Chỉ cần kiểm tra username và password
        if (u.isEmpty() || p.isEmpty()) {
            lblMessage.setText("Vui lòng điền đầy đủ thông tin đăng ký!");
            return;
        }

        // =======================================================
        // CẬP NHẬT LOGIC: Kiểm tra nút RadioButton nào đang được chọn
        // =======================================================
        User newUser;
        if (rbSeller != null && rbSeller.isSelected()) {
            newUser = new Seller(u, p); // Khởi tạo đối tượng Seller nếu tích chọn Seller
        } else {
            newUser = new Bidder(u, p);  // Mặc định tạo đối tượng người mua (Bidder)
        }

        // Gửi dữ liệu lên Database thông qua userDAO
        boolean isSuccess = userDAO.registerUser(newUser, p);

        if (isSuccess) {
            lblMessage.setText("Đăng ký thành công! Mời bạn đăng nhập.");
            // Điền sẵn tên tài khoản vừa tạo sang ô đăng nhập để tiện sử dụng
            txtUsername.setText(u);
            txtPassword.clear();

            handleSwitch(); // Tự động trượt về màn hình đăng nhập
        } else {
            lblMessage.setText("Đăng ký thất bại! Tên tài khoản đã tồn tại.");
        }
    }

    // Các hàm hỗ trợ mở giao diện riêng biệt
    private void openSellerDashboard() {
        loadDashboard("/resources/SellerDashboard.fxml", "Seller Dashboard");
    }

    private void openBidderDashboard() {
        loadDashboard("/resources/BidderDashboard.fxml", "Bidder Dashboard");
    }

    private void openAdminDashboard() {
        loadDashboard("/resources/AdminDashboard.fxml", "Admin Dashboard");
    }

    /**
     * Hàm dùng chung để tải (load) một file FXML mới và hiển thị lên Stage hiện tại
     * @param fxmlPath Đường dẫn tới file giao diện
     * @param title Tiêu đề cửa sổ
     */
    private void loadDashboard(String fxmlPath, String title) {
        try {
            // Tìm file FXML
            URL location = getClass().getResource(fxmlPath);
            // Fallback: nếu không tìm thấy trong /resources, thử tìm trực tiếp
            if (location == null) {
                location = getClass().getResource(fxmlPath.replace("/resources", ""));
            }

            if (location == null) {
                lblMessage.setText("Không tìm thấy file FXML!");
                return;
            }

            // Tải giao diện mới
            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            // Lấy Stage (cửa sổ) hiện tại từ bất kỳ component nào (ở đây dùng txtUsername)
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600)); // Thiết lập kích thước màn hình dashboard
            stage.setTitle(title);
            stage.centerOnScreen(); // Đưa cửa sổ ra giữa màn hình
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi mở Dashboard: " + fxmlPath, e);
            lblMessage.setText("Lỗi hệ thống không thể tải trang!");
        }
    }
}