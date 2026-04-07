package com.example.logincontroller.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminDashboardController {

    @FXML
    private TableView<?> auctionTable;

    @FXML
    private TableColumn<?, ?> colId;

    @FXML
    private TableColumn<?, ?> colStatus;

    @FXML
    private void handleView() {
        System.out.println("Admin xem chi tiết phiên đấu giá");
    }

    @FXML
    private void handleStop() {
        System.out.println("Admin dừng phiên đấu giá");
    }
}