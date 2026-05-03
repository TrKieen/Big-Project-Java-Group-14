package AuctionSystem.controller;

import AuctionSystem.model.auction.Auction;
import AuctionSystem.model.auction.AuctionObserver;
import AuctionSystem.model.user.Bidder;
import AuctionSystem.network.NetworkClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class BidderDashboardController implements AuctionObserver {
    @FXML
    private TableView<Auction> itemTable;

    @FXML
    private TableColumn<Auction, String> colName;

    @FXML
    private TableColumn<Auction, Double> colPrice;

    @FXML
    private TextField txtBidPrice;

    private final NetworkClient networkClient = new NetworkClient();
    private ObservableList<Auction> auctionList;
    private Auction selectedAuction;
    private Bidder currentBidder;

    @FXML
    public void initialize() {
        currentBidder = new Bidder("sinhvienUET", "123");

        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getItem().getCurrentHighestPrice()));

        auctionList = FXCollections.observableArrayList(networkClient.sendGetAuctionsRequest());
        itemTable.setItems(auctionList);

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedAuction = newSelection;
        });

        for (Auction auction : auctionList) {
            auction.addObserver(this);
        }
    }

    @FXML
    private void handleBid() {
        if (selectedAuction == null) {
            showAlert("Cảnh báo", "Vui lòng chọn một sản phẩm trên bảng để đấu giá!", Alert.AlertType.WARNING);
            return;
        }

        String priceInput = txtBidPrice.getText().trim();
        if (priceInput.isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập mức giá!", Alert.AlertType.WARNING);
            return;
        }

        try {
            double bidAmount = Double.parseDouble(priceInput);
            currentBidder.placeBid(selectedAuction, bidAmount);
            showAlert("Thành công", "Bạn đã đặt giá " + bidAmount + " thành công.", Alert.AlertType.INFORMATION);
            txtBidPrice.clear();
        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Vui lòng chỉ nhập số!", Alert.AlertType.ERROR);
        } catch (RuntimeException e) {
            showAlert("Lỗi đặt giá", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        Platform.runLater(() -> {
            itemTable.refresh();
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}