package AuctionSystem.controller;

import AuctionSystem.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import AuctionSystem.model.auction.Auction;
import AuctionSystem.model.auction.AuctionManager;
import AuctionSystem.model.auction.AuctionObserver;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AdminDashboardController implements AuctionObserver {

    @FXML
    private TableView<Auction> auctionTable;

    @FXML
    private TableColumn<Auction, String> colId;

    @FXML
    private TableColumn<Auction, String> colStatus;

    private final NetworkClient networkClient = new NetworkClient();
    private ObservableList<Auction> auctionList;
    private Auction selectedAuction;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));

        auctionList = FXCollections.observableArrayList(networkClient.sendGetAuctionsRequest());
        auctionTable.setItems(auctionList);

        auctionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedAuction = newVal;
        });

        for (Auction auction : auctionList) {
            auction.addObserver(this);
        }
    }

    @FXML
    private void handleView() {
        if (selectedAuction != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chi tiết phiên đấu giá");
            alert.setHeaderText("Sản phẩm: " + selectedAuction.getItem().getName());

            String winner = selectedAuction.getLeadingBidder() != null ? selectedAuction.getLeadingBidder().getUsername() : "Chưa có ai đặt giá";
            alert.setContentText("Trạng thái: " + selectedAuction.getStatus() +
                    "\nMức giá cao nhất: " + selectedAuction.getItem().getCurrentHighestPrice() +
                    "\nNgười đang dẫn đầu: " + winner);
            alert.showAndWait();
        }
    }

    @FXML
    private void handleStop() {
        if (selectedAuction != null) {
            AuctionManager.getInstance().closeAuction(selectedAuction);
        }
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        Platform.runLater(() -> {
            auctionTable.refresh();
        });
    }
}
