package com.example.logincontroller.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class BidderDashboardController {

    @FXML
    private TableView<?> itemTable;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableColumn<?, ?> colPrice;

    @FXML
    private TextField txtBidPrice;

    @FXML
    private void handleBid() {
        String price = txtBidPrice.getText();
        System.out.println("Bidder đặt giá: " + price);
    }
}