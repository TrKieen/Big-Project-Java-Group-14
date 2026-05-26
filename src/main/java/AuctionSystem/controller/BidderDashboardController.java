package AuctionSystem.controller;

import AuctionSystem.model.auction.Auction;
import AuctionSystem.model.auction.AuctionObserver;
import AuctionSystem.model.auction.Bid;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class BidderDashboardController implements AuctionObserver {

    @FXML private TableView<Auction> itemTable;
    @FXML private TableColumn<Auction, String> colId;
    @FXML private TableColumn<Auction, String> colName;
    @FXML private TableColumn<Auction, Double> colPrice;
    @FXML private TableColumn<Auction, String> colStatus;

    @FXML private Label lblDetailName;
    @FXML private Label lblDetailDesc;
    @FXML private Label lblStartPrice;

    // ===== BỔ SUNG THÊM 4 BIẾN ĐỂ HIỂN THỊ THÔNG SỐ KHỐI BÊN PHẢI =====
    @FXML private Label lblSessionId;
    @FXML private Label lblLeaderUser;
    @FXML private Label lblSessionStatus;
    @FXML private Label lblTimeRemaining;

    @FXML private TextField txtBidPrice;
    @FXML private TextField txtMaxBid;
    @FXML private TextField txtIncrement;

    @FXML private LineChart<String, Number> priceChart;
    @FXML private Axis<String> xAxisTimeline;
    @FXML private NumberAxis yAxisPrice;

    private final NetworkClient networkClient = new NetworkClient();
    private final ObservableList<Auction> auctionList = FXCollections.observableArrayList();

    private Auction selectedAuction;
    private Bidder currentBidder;

    private final Map<String, List<XYChart.Data<String, Number>>> chartRawDataMap = new HashMap<>();
    private XYChart.Series<String, Number> activeSeries;

    private Timeline countdownTimeline;

    @FXML
    public void initialize() {
        priceChart.setAnimated(false);
        activeSeries = new XYChart.Series<>();
        priceChart.getData().add(activeSeries);

        String username = UserSession.getInstance().getUsername();
        currentBidder = new Bidder(username, null);

        colId.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItem().getId()));
        colName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItem().getName()));
        colPrice.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getItem().getCurrentHighestPrice()));
        colStatus.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus().toString()));

        itemTable.setItems(auctionList);
        loadAuctions();

        itemTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedAuction = newVal;
                    updateDetailPanel(newVal);
                    updateChartDisplay(newVal);
                });

        // Bổ sung cập nhật nhãn thời gian thực mỗi giây cho phiên đang chọn
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    itemTable.refresh();
                    if (selectedAuction != null) {
                        lblTimeRemaining.setText(selectedAuction.getTimeRemainingFormatted());
                    }
                }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void loadAuctions() {
        List<Auction> fresh = networkClient.sendGetAuctionsRequest();
        if (fresh == null) fresh = new ArrayList<>();

        for (Auction a : auctionList) {
            a.removeObserver(this);
        }

        auctionList.setAll(fresh);

        for (Auction a : auctionList) {
            a.addObserver(this);
            rebuildChartDataFromHistory(a);
        }

        chartRawDataMap.keySet().retainAll(
                auctionList.stream()
                        .map(a -> a.getItem().getId())
                        .toList()
        );
    }

    private void rebuildChartDataFromHistory(Auction auction) {
        if (auction == null || auction.getItem() == null) return;

        String id = auction.getItem().getId();
        List<XYChart.Data<String, Number>> points = new ArrayList<>();

        double startPrice = auction.getItem().getStartingPrice();
        points.add(new XYChart.Data<>("Bắt đầu", startPrice));

        List<Bid> history = auction.getBidHistory();
        if (history != null) {
            for (int i = 0; i < history.size(); i++) {
                points.add(new XYChart.Data<>("Lượt " + (i + 1), history.get(i).getAmount()));
            }
        }

        while (points.size() > 20) {
            points.remove(1);
        }

        chartRawDataMap.put(id, points);
    }

    // ===== PHẦN ĐÃ SỬA: ĐỔ DỮ LIỆU ĐỘNG VÀO CẢ KHỐI THÔNG SỐ PHẢI =====
    private void updateDetailPanel(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            lblDetailName.setText("Chưa chọn sản phẩm");
            lblDetailDesc.setText("...");
            lblStartPrice.setText("0 VND");

            lblSessionId.setText("--");
            lblLeaderUser.setText("--");
            lblSessionStatus.setText("--");
            lblTimeRemaining.setText("--:--:--");
            return;
        }

        // 1. Cập nhật khối bên trái (Tên, mô tả, giá hiện tại)
        lblDetailName.setText(auction.getItem().getName());
        lblDetailDesc.setText(
                auction.getItem().getDescription() != null
                        ? auction.getItem().getDescription()
                        : "Không có mô tả.");

        lblStartPrice.setText(String.format("%,.0f VND",
                auction.getItem().getCurrentHighestPrice()));

        // 2. Cập nhật khối thông số bên phải (ID, Người dẫn đầu, Trạng thái, Thời gian)
        lblSessionId.setText(auction.getItem().getId());

        Bidder b = auction.getLeadingBidder();
        lblLeaderUser.setText(b != null ? b.getUsername() : "Chưa có");

        lblSessionStatus.setText(auction.getStatus().toString());
        lblTimeRemaining.setText(auction.getTimeRemainingFormatted());
    }

    private void updateChartDisplay(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            activeSeries.getData().clear();
            return;
        }
        yAxisPrice.setAutoRanging(true);
        // 1. Xóa các danh mục cũ trên trục X để chống giật/chồng chéo
        if (xAxisTimeline instanceof CategoryAxis axis) {
            axis.getCategories().clear();
        }

        // 2. Kích hoạt lại tính năng tự động tính toán khoảng giá cho trục Y
        yAxisPrice.setAutoRanging(true);

        // 3. Làm mới dữ liệu từ lịch sử đấu giá trong bộ nhớ
        rebuildChartDataFromHistory(auction);
        activeSeries.setName(auction.getItem().getName());

        // 4. Đổ dữ liệu mới vào đồ thị
        activeSeries.getData().setAll(
                new ArrayList<>(chartRawDataMap.get(auction.getItem().getId())));
    }

    @FXML
    private void handleBid() {
        if (selectedAuction == null) return;

        String input = txtBidPrice.getText().trim();
        if (input.isEmpty()) {
            showAlert("Thiếu dữ liệu", "Vui lòng nhập giá đấu!", Alert.AlertType.WARNING);
            return;
        }

        try {
            double bid = Double.parseDouble(input);
            double current = selectedAuction.getItem().getCurrentHighestPrice();

            if (bid <= current || selectedAuction.isClosed()) {
                showAlert("Lỗi", "Giá phải lớn hơn giá hiện tại!", Alert.AlertType.WARNING);
                return;
            }

            boolean success = networkClient.sendPlaceBidRequest(
                    selectedAuction.getItem().getId(),
                    currentBidder.getUsername(),
                    bid
            );

            if (success) {
                txtBidPrice.clear();
                handleRefresh();
            } else {
                showAlert("Thất bại", "Đặt giá không thành công.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Giá không hợp lệ!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAutoBid() {
        if (selectedAuction == null) return;

        String maxStr = txtMaxBid.getText().trim();
        String incStr = txtIncrement.getText().trim();

        if (maxStr.isEmpty() || incStr.isEmpty()) {
            showAlert("Thiếu dữ liệu", "Vui lòng nhập đầy đủ Auto-Bid.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double max = Double.parseDouble(maxStr);
            double inc = Double.parseDouble(incStr);
            double current = selectedAuction.getItem().getCurrentHighestPrice();

            if (max <= current) {
                showAlert("Lỗi", "Giá tối đa phải lớn hơn giá hiện tại!", Alert.AlertType.WARNING);
                return;
            }
            if (inc <= 0) {
                showAlert("Lỗi", "Bước giá phải lớn hơn 0!", Alert.AlertType.WARNING);
                return;
            }

            boolean success = networkClient.sendRegisterAutoBidRequest(
                    selectedAuction.getItem().getId(),
                    currentBidder.getUsername(),
                    max,
                    inc
            );

            if (success) {
                txtMaxBid.clear();
                txtIncrement.clear();
                showAlert("Thành công", "Đã bật Auto-Bid!", Alert.AlertType.INFORMATION);
                handleRefresh();
            } else {
                showAlert("Thất bại", "Không thể đăng ký Auto-Bid.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Dữ liệu Auto-Bid không hợp lệ!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        String lastId = selectedAuction != null ? selectedAuction.getItem().getId() : null;
        loadAuctions();

        if (lastId != null) {
            auctionList.stream()
                    .filter(a -> a.getItem().getId().equals(lastId))
                    .findFirst()
                    .ifPresentOrElse(a -> {
                        selectedAuction = a;
                        itemTable.getSelectionModel().select(a);
                        updateDetailPanel(a);
                        updateChartDisplay(a);
                    }, () -> {
                        selectedAuction = null;
                        itemTable.getSelectionModel().clearSelection();
                        priceChart.getData().clear();
                        showAlert("Thông báo", "Sản phẩm đã bị gỡ khỏi sàn.", Alert.AlertType.INFORMATION);
                    });
        }
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        if (auction == null || auction.getItem() == null) return;

        Platform.runLater(() -> {
            String updatedId = auction.getItem().getId();

            for (int i = 0; i < auctionList.size(); i++) {
                if (auctionList.get(i).getItem().getId().equals(updatedId)) {
                    auctionList.set(i, auction);
                    break;
                }
            }

            itemTable.refresh();

            if (selectedAuction != null &&
                    selectedAuction.getItem().getId().equals(updatedId)) {

                selectedAuction = auction;
                updateDetailPanel(auction);
                updateChartDisplay(auction);
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn đăng xuất?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (countdownTimeline != null) {
                    countdownTimeline.stop();
                }
                FXMLLoader loader =
                        new FXMLLoader(getClass().getResource("/hello-view.fxml"));
                Parent root = loader.load();

                Stage stage =
                        (Stage) ((Node) event.getSource())
                                .getScene().getWindow();

                // 1. Tạo Scene mới
                Scene newScene = new Scene(root);
                stage.setScene(newScene);
                stage.setTitle("Đăng nhập");

                // 2. Ép cửa sổ tính toán lại layout toàn màn hình cho giao diện mới
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                } else {
                    stage.setMaximized(true);
                }

                // 2. THÊM 2 DÒNG NÀY ĐỂ CHỐNG LỆCH:
                stage.sizeToScene();      // Ép cửa sổ thu/phóng khít theo kích thước màn hình Đăng nhập mới
                stage.centerOnScreen();   // Đưa toàn bộ cửa sổ về chính giữa màn hình máy tính

                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}