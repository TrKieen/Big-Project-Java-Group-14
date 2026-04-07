package com.example.logincontroller.controller;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.logincontroller.model.user.Admin;
import com.example.logincontroller.model.user.Bidder;
import com.example.logincontroller.model.user.Seller;
import com.example.logincontroller.model.user.User;
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
        String u = txtUsername.getText();
        String p = txtPassword.getText();

        User user = null;

        if (u.equals("seller")) {
            user = new Seller(u, "123");
        } else if (u.equals("bidder")) {
            user = new Bidder(u, "123");
        } else if (u.equals("admin")) {
            user = new Admin(u, "123");
        }

        if (user != null && user.login(p)) {
            switch (user.getRole()) {
                case "SELLER" -> openSellerDashboard();
                case "BIDDER" -> openBidderDashboard();
                case "ADMIN"  -> openAdminDashboard();
            }
        } else {
            lblMessage.setText("Sai tài khoản hoặc mật khẩu");
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
    private void openBidderDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BidderDashboard.fxml"));
            if(loader.getLocation() == null) {
                loader = new FXMLLoader(getClass().getResource("/resources/BidderDashboard.fxml"));
            }
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Tham gia đấu giá - Bidder");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tải giao diện BidderDashboard.fxml", e);
            lblMessage.setText("Lỗi tải giao diện Dashboard!");
        }
    }

    private void openAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            if(loader.getLocation() == null) {
                loader = new FXMLLoader(getClass().getResource("/resources/AdminDashboard.fxml"));
            }
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Quản trị hệ thống - Admin");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tải giao diện AdminDashboard.fxml", e);
            lblMessage.setText("Lỗi tải giao diện Dashboard!");
        }
    }

    private void openScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
        } catch (IOException e) {
            lblMessage.setText("Không mở được giao diện!");
            e.printStackTrace();
        }
    }
}

