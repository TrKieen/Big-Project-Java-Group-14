package AuctionSystem.controller;

import AuctionSystem.model.auction.Auction;
import AuctionSystem.model.auction.AuctionObserver;
import AuctionSystem.model.user.Bidder;
import AuctionSystem.model.user.UserSession;
import AuctionSystem.network.NetworkClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

public class BidderDashboardController implements AuctionObserver {
    @FXML
    private TableView<Auction> itemTable;

    @FXML
    private TableColumn<Auction, String> colName;

    @FXML
    private TableColumn<Auction, Double> colPrice;

    @FXML
    private TextField txtBidPrice;

    @FXML
    private TableColumn<Auction, String> colId;

    @FXML
    private TableColumn<Auction, String> colHighestBidder;

    @FXML
    private TableColumn<Auction, String> colStatus;

    @FXML
    private TableColumn<Auction, String> colTime;

    private final NetworkClient networkClient = new NetworkClient();
    private ObservableList<Auction> auctionList;
    private Auction selectedAuction;
    private Bidder currentBidder;

    @FXML
    public void initialize() {
        String currentUsername = UserSession.getInstance().getUsername();
        currentBidder = new Bidder(currentUsername != null ? currentUsername : "bidder", "123");

        // 1. Đổ dữ liệu ID và Tên sản phẩm từ thực thể Item
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getId()));
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getItem().getCurrentHighestPrice()));

        // 2. Cột người dẫn đầu (Fix lỗi trả về Object Bidder)
        colHighestBidder.setCellValueFactory(cellData -> {
            Bidder leadingBidder = cellData.getValue().getLeadingBidder();
            // Nếu chưa có ai đặt giá thì hiển thị "Chưa có", có rồi thì lấy Username của họ
            String bidderName = (leadingBidder != null) ? leadingBidder.getUsername() : "Chưa có";
            return new SimpleStringProperty(bidderName);
        });

        // 3. Cột trạng thái (Fix lỗi trả về Enum AuctionStatus)
        colStatus.setCellValueFactory(cellData -> {
            String statusStr = cellData.getValue().getStatus().toString(); // Chuyển Enum sang String (OPEN, RUNNING, FINISHED)
            return new SimpleStringProperty(statusStr);
        });

        // 4. Cột thời gian còn lại (Sử dụng hàm format đếm ngược có sẵn trong Auction.java)
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeRemainingFormatted()));

        loadAuctions();

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedAuction = newSelection;
        });
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    itemTable.refresh(); // Ép TableView vẽ lại giao diện mỗi giây để cập nhật cột thời gian
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Chạy vô hạn
        timeline.play(); // Kích hoạt bộ đếm
    }

    private void loadAuctions() {
        auctionList = FXCollections.observableArrayList(networkClient.sendGetAuctionsRequest());
        itemTable.setItems(auctionList);

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
            double currentPrice = selectedAuction.getItem().getCurrentHighestPrice();

            if (bidAmount <= currentPrice) {
                showAlert("Lỗi đặt giá", "Giá bạn nhập phải cao hơn giá hiện tại!", Alert.AlertType.WARNING);
                return;
            }

            if (selectedAuction.isClosed()) {
                showAlert("Phiên đấu giá đã kết thúc!", "Không thể đặt giá.", Alert.AlertType.WARNING);
                return;
            }

            // --- SỬA ĐOẠN NÀY: Gửi yêu cầu đặt giá lên Server thay vì làm cục bộ ---
            boolean success = networkClient.sendPlaceBidRequest(
                    selectedAuction.getItem().getId(),
                    currentBidder.getUsername(), // Giả định class Bidder của bạn có hàm getUsername()
                    bidAmount
            );

            if (success) {
                // Cập nhật tạm thời giá trị trên giao diện người đặt cho khớp
                selectedAuction.getItem().setCurrentHighestPrice(bidAmount);
                itemTable.refresh();

                showAlert("Thành công", "Bạn đã đặt giá " + bidAmount + " thành công.", Alert.AlertType.INFORMATION);
                txtBidPrice.clear();
            } else {
                showAlert("Thất bại", "Đặt giá không thành công từ phía Server!", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Vui lòng chỉ nhập số!", Alert.AlertType.ERROR);
        } catch (RuntimeException e) {
            showAlert("Lỗi đặt giá", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        loadAuctions();
        showAlert("Thông báo", "Danh sách đấu giá đã được cập nhật!", Alert.AlertType.INFORMATION);
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        Platform.runLater(() -> {
            itemTable.refresh();
            System.out.println("Cập nhật đấu giá: " + auction.getItem().getName() +
                    " - Giá mới: " + auction.getItem().getCurrentHighestPrice());
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
