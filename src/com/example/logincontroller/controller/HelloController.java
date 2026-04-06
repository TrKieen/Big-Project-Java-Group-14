package com.example.logincontroller.controller;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloController {
    private static final Logger LOGGER = Logger.getLogger(HelloController.class.getName());
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMessage;

    @FXML
    protected void onLoginButtonClick() {
        String user = this.txtUsername.getText();
        String pass = this.txtPassword.getText();
        if (user.equals("admin") && pass.equals("123")) {
            this.lblMessage.setText("Đăng nhập thành công!");
            this.lblMessage.setStyle("-fx-text-fill: green;");
            openSellerDashboard();
        } else {
            this.lblMessage.setText("Sai tài khoản hoặc mật khẩu!");
            this.lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    private void openSellerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SellerDashboard.fxml"));
            if(loader.getLocation() == null) {
                loader = new FXMLLoader(getClass().getResource("/resources/SellerDashboard.fxml"));
            }
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Quản lý sản phẩm đấu giá - Seller");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tải giao diện SellerDashboard.fxml", e);
            lblMessage.setText("Lỗi tải giao diện Dashboard!");
        }
    }
}

