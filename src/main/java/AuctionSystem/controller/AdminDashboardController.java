package AuctionSystem.controller;

import AuctionSystem.model.auction.Bid;
import AuctionSystem.network.NetworkClient;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
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

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    // Đổi biến timeline thành biến toàn cục của class để hàm logout có thể tiếp cận và dừng nó lại
    private Timeline timeline;

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
        timeline = new Timeline(
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

    @FXML
    private void handleLogout(ActionEvent event) {
        // 1. Hộp thoại xác nhận đăng xuất từ hệ thống
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống quản trị?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            // 2. Bắt buộc phải dừng timeline chạy ngầm để tránh lỗi xung đột luồng giao diện JavaFX khi đổi Scene
            if (timeline != null) {
                timeline.stop();
            }

            try {
                // 3. Gọi file hello-view.fxml (màn hình Đăng nhập) từ gốc thư mục tài nguyên (resources)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/hello-view.fxml"));
                Parent root = loader.load();

                // 4. Lấy Stage hiện tại từ chính phần tử nút bấm kích hoạt ActionEvent này
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // 5. Cài đặt Scene đăng nhập mới và căn giữa lại trên màn hình máy tính
                currentStage.setScene(new Scene(root));
                currentStage.setTitle("Đăng nhập hệ thống");
                currentStage.centerOnScreen();
                currentStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Lỗi: Không tìm thấy file hello-view.fxml.");
            }
        }
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
            auctionTable.refresh();

            // 3. Nếu Admin đang click chọn xem đúng sản phẩm vừa có người đặt giá này -> Vẽ tiếp điểm lên biểu đồ
            if (selectedAuction != null && Objects.equals(selectedAuction.getItem().getId(), auction.getItem().getId())) {
                selectedAuction = auction; // Đồng bộ lại đối tượng đang chọn

                List<Bid> history = auction.getBidHistory();
                if (!history.isEmpty()) {
                    String labelNow = "Lượt " + history.size();
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