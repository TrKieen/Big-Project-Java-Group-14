package AuctionSystem.controller;

import AuctionSystem.model.auction.Bid;
import AuctionSystem.network.NetworkClient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import AuctionSystem.model.auction.Auction;
import AuctionSystem.model.auction.AuctionObserver;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController implements AuctionObserver {

    @FXML
    private TableView<Auction> auctionTable;
    @FXML
    private TableColumn<Auction, String> colId;
    @FXML
    private TableColumn<Auction, String> colItemName;
    @FXML
    private TableColumn<Auction, Double> colCurrentBid;
    @FXML
    private TableColumn<Auction, String> colBidder;
    @FXML
    private TableColumn<Auction, String> colStatus;
    @FXML
    private TableColumn<Auction, String> colTimeLeft;

    @FXML
    private LineChart<String, Number> priceChart;
    @FXML
    private Label statusLabel;

    private final NetworkClient networkClient = new NetworkClient();
    private ObservableList<Auction> auctionList;
    private Auction selectedAuction;

    // Series dữ liệu cho biểu đồ
    private XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();

    @FXML
    public void initialize() {
        // 1. Đồng bộ các cột TableView với Model [cite: 14, 17]
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getItem().getId())));
        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colCurrentBid.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getItem().getCurrentHighestPrice()).asObject());
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        colBidder.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getLeadingBidder() != null ? cellData.getValue().getLeadingBidder().getUsername() : "---"
        ));
        colTimeLeft.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeRemainingFormatted()));

        // 2. Khởi tạo danh sách và dữ liệu mạng [cite: 102]
        auctionList = FXCollections.observableArrayList(networkClient.sendGetAuctionsRequest());
        auctionTable.setItems(auctionList);

        // 3. Thiết lập biểu đồ
        priceSeries.setName("Biến động giá");
        priceChart.getData().add(priceSeries);

        // Lắng nghe sự kiện chọn dòng trong bảng
        auctionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedAuction = newVal;
            if (newVal != null) {
                updateChartForSelectedAuction(newVal);
            }
        });

        colTimeLeft.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTimeRemainingFormatted())
        );


        // Đăng ký Observer cho từng phiên đấu giá [cite: 23, 66]
        for (Auction auction : auctionList) {
            auction.addObserver(this);
        }
    }

    private void updateChartForSelectedAuction(Auction auction) {
        priceSeries.getData().clear();
        List<Bid> history = auction.getBidHistory(); // Lấy dữ liệu thực từ Model

        for (int i = 0; i < history.size(); i++) {
            priceSeries.getData().add(new XYChart.Data<>("Lượt " + (i + 1), history.get(i).getPrice()));
        }
        statusLabel.setText("Đang theo dõi: " + auction.getItem().getName());
    }

    @FXML
    private void handleView() {
        if (selectedAuction != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chi tiết phiên đấu giá");
            alert.setHeaderText("Sản phẩm: " + selectedAuction.getItem().getName());

            String info = String.format("Trạng thái: %s\nGiá hiện tại: %.2f\nNgười dẫn đầu: %s",
                    selectedAuction.getStatus(),
                    selectedAuction.getItem().getCurrentHighestPrice(),
                    selectedAuction.getLeadingBidder() != null ? selectedAuction.getLeadingBidder().getUsername() : "Chưa có"
            );
            alert.setContentText(info);
            alert.showAndWait();
        }
    }

    @FXML
    private void handleStop() {
        if (selectedAuction != null) {
            boolean success = networkClient.sendStopAuctionRequest(String.valueOf(selectedAuction.getItem().getId()));
            if (success) {
                statusLabel.setText("Thông báo: Đã gửi lệnh dừng phiên " + selectedAuction.getItem().getId());
            }
        }
    }

    @FXML
    private void handleCreate() {
        // Logic mở popup tạo phiên đấu giá mới [cite: 17]
        System.out.println("Mở màn hình tạo phiên mới...");
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        // Cập nhật UI an toàn từ luồng khác (Socket) [cite: 71, 125]
        Platform.runLater(() -> {
            auctionTable.refresh();

            // Nếu phiên đang cập nhật là phiên đang chọn, thì vẽ lên biểu đồ
            if (selectedAuction != null && selectedAuction.getItem().getId() == auction.getItem().getId()) {
                String timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                priceSeries.getData().add(new XYChart.Data<>(timeNow, auction.getItem().getCurrentHighestPrice()));

                // Giới hạn 20 điểm dữ liệu để tránh lag biểu đồ
                if (priceSeries.getData().size() > 20) {
                    priceSeries.getData().remove(0);
                }
            }
        });
    }
}