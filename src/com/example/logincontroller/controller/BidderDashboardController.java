package com.example.logincontroller.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class BidderDashboardController {

    @FXML
    private TextField txtBidPrice;

    @FXML
    private void handleBid() {
        String priceInput = txtBidPrice.getText().trim();
        if (priceInput.isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập số tiền bạn muốn đấu giá!", AlertType.WARNING);
            return;
        }

        try {
            double bidAmount = Double.parseDouble(priceInput);
            double currentPrice = 100.0;

            if (bidAmount > currentPrice) {
                System.out.println("Bidder đã đặt giá thành công: " + bidAmount);
                showAlert("Thành công", "Chúc mừng! Bạn đã đặt giá " + bidAmount + " thành công.", AlertType.INFORMATION);
                txtBidPrice.clear();
            } else {
                showAlert("Lỗi đặt giá", "Giá đặt phải cao hơn giá hiện tại (" + currentPrice + ")!", AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Vui lòng chỉ nhập số, không nhập chữ!", AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}