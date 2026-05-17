package AuctionSystem.controller;

import AuctionSystem.model.auction.Bid;
import AuctionSystem.network.NetworkClient;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

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

    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        // 1. Đồng bộ các cột TableView với Model
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getItem().getId())));
        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colCurrentBid.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getItem().getCurrentHighestPrice()).asObject());
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        colBidder.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getLeadingBidder() != null ? cellData.getValue().getLeadingBidder().getUsername() : "---"
        ));
        colTimeLeft.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeRemainingFormatted()));

        // 2. Khởi tạo danh sách và dữ liệu mạng
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

        // 4. Đăng ký Observer cho từng phiên đấu giá
        for (Auction auction : auctionList) {
            auction.addObserver(this);
        }
        // Tạo một Timeline chạy định kỳ mỗi 1 giây để ép TableView cập nhật lại cột thời gian
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    // Lệnh này ép TableView vẽ lại giao diện dựa trên dữ liệu thời gian mới nhất
                    auctionTable.refresh();
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Chạy vô hạn
        timeline.play(); // Kích hoạt bộ đếm thời gian
    }

    private void updateChartForSelectedAuction(Auction auction) {
        priceSeries.getData().clear();
        List<Bid> history = auction.getBidHistory();

        // Giả sử mỗi Bid có hàm getTime() trả về LocalTime hoặc chuỗi thời gian.
        // Nếu không có, bạn có thể tạm thời dùng "Lượt " + (i + 1) nhưng nhớ sửa cả hàm onAuctionUpdated bên dưới nhé.
        for (int i = 0; i < history.size(); i++) {
            String label = "Lượt " + (i + 1);
            priceSeries.getData().add(new XYChart.Data<>(label, history.get(i).getPrice()));
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
        System.out.println("Mở màn hình tạo phiên mới...");
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        Platform.runLater(() -> {
            // 1. Cập nhật đối tượng phiên đấu giá mới vào danh sách hiển thị trên bảng công khai
            for (int i = 0; i < auctionList.size(); i++) {
                if (Objects.equals(auctionList.get(i).getItem().getId(), auction.getItem().getId())) {
                    auctionList.set(i, auction);
                    break;
                }
            }

            // 2. Ép bảng làm mới lập tức -> Cột "Người dẫn đầu" (colBidder) sẽ tự động hiển thị tên
            // vì hàm getLeadingBidder() trong file Auction.java của bạn đọc từ phần tử cuối của bidHistory!
            auctionTable.refresh();

            // 3. Nếu Admin đang click chọn xem đúng sản phẩm vừa có người đặt giá này -> Vẽ tiếp điểm lên biểu đồ
            if (selectedAuction != null && Objects.equals(selectedAuction.getItem().getId(), auction.getItem().getId())) {
                selectedAuction = auction; // Đồng bộ lại đối tượng đang chọn

                List<Bid> history = auction.getBidHistory();
                if (!history.isEmpty()) {
                    // Nhãn hiển thị trục X (Ví dụ: Lượt 1, Lượt 2, Lượt 3...)
                    String labelNow = "Lượt " + history.size();

                    // Lấy số tiền cao nhất vừa đặt (Khớp với hàm getCurrentHighestPrice() của Item)
                    double currentPrice = auction.getItem().getCurrentHighestPrice();

                    // Thêm điểm đồ thị mới vào biểu đồ đường
                    priceSeries.getData().add(new XYChart.Data<>(labelNow, currentPrice));

                    // Giới hạn đồ thị chỉ hiển thị tối đa 20 lượt bid gần nhất để giao diện không bị rối
                    if (priceSeries.getData().size() > 20) {
                        priceSeries.getData().remove(0);
                    }

                    // Cập nhật dòng trạng thái góc dưới màn hình
                    Bid latestBid = history.get(history.size() - 1);
                    statusLabel.setText("Theo dõi: " + auction.getItem().getName() + " | Người dẫn đầu: " + latestBid.getBidder().getUsername());
                }
            }
        });
    }
}