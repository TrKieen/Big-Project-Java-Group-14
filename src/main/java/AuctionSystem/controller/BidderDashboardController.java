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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BidderDashboardController implements AuctionObserver {

    @FXML private TableView<Auction> itemTable;
    @FXML private TableColumn<Auction, String> colId;
    @FXML private TableColumn<Auction, String> colName;
    @FXML private TableColumn<Auction, Double> colPrice;
    @FXML private TableColumn<Auction, String> colStatus;
    @FXML private TableColumn<Auction, String> colTime;

    @FXML private Button btnMenuAuction;
    @FXML private Button btnMenuHistory;
    @FXML private Button btnLogout;

    @FXML private Label lblUsername;
    @FXML private Label lblItemType;
    @FXML private Label lblDetailName;
    @FXML private Label lblDetailDesc;
    @FXML private Label lblSpecificSpecs;
    @FXML private Label lblHighestBidder;
    @FXML private Label lblStartPrice;

    @FXML private TextField txtBidPrice;
    @FXML private TextField txtMaxBid;
    @FXML private TextField txtIncrement;
    @FXML private Button btnSubmitBid;
    @FXML private Button btnToggleAutoBid;
    @FXML private Label lblStatusMessage;

    // ĐỒ THỊ BIẾN ĐỘNG GIÁ LINECHART CHUẨN ĐƠN TRỤC NHƯ ADMIN
    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis xAxisTimeline;
    @FXML private NumberAxis yAxisPrice;

    private final NetworkClient networkClient = new NetworkClient();
    private ObservableList<Auction> auctionList;
    private Auction selectedAuction;
    private Bidder currentBidder;

    private final Map<String, List<XYChart.Data<String, Number>>> chartRawDataMap = new HashMap<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private XYChart.Series<String, Number> activeSeries;

    @FXML
    public void initialize() {
        priceChart.setAnimated(false);
        xAxisTimeline.setAnimated(false);
        yAxisPrice.setAnimated(false);

        activeSeries = new XYChart.Series<>();
        priceChart.getData().add(activeSeries);

        String currentUsername = UserSession.getInstance().getUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty()) {
            lblUsername.setText(currentUsername);
            currentBidder = new Bidder(currentUsername, "123");
        } else {
            lblUsername.setText("Khách (Bidder)");
            currentBidder = new Bidder("bidder_guest", "123");
        }

        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getId()));
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getItem().getCurrentHighestPrice()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeRemainingFormatted()));

        loadAuctions();

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedAuction = newSelection;
            updateDetailPanel(newSelection);
            updateChartDisplay(newSelection);
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> itemTable.refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            lblUsername.setText(username);
            currentBidder = new Bidder(username, "123");
        }
    }

    private void loadAuctions() {
        List<Auction> activeAuctions = networkClient.sendGetAuctionsRequest();
        if (activeAuctions == null) activeAuctions = new ArrayList<>();

        auctionList = FXCollections.observableArrayList(activeAuctions);
        itemTable.setItems(auctionList);

        for (Auction auction : auctionList) {
            auction.addObserver(this);
            String auctionId = auction.getItem().getId();

            if (!chartRawDataMap.containsKey(auctionId)) {
                List<XYChart.Data<String, Number>> dataPoints = new ArrayList<>();
                String nowStr = LocalTime.now().format(timeFormatter);
                dataPoints.add(new XYChart.Data<>(nowStr, auction.getItem().getCurrentHighestPrice()));
                chartRawDataMap.put(auctionId, dataPoints);
            }
        }
    }

    private void updateDetailPanel(Auction auction) {
        if (auction != null) {
            lblDetailName.setText(auction.getItem().getName());
            lblDetailDesc.setText(auction.getItem().getDescription() != null ? auction.getItem().getDescription() : "Không có mô tả sản phẩm.");
            lblStartPrice.setText(String.format("%,.0f VND", auction.getItem().getCurrentHighestPrice()));
            lblHighestBidder.setText(auction.getLeadingBidder() != null ? auction.getLeadingBidder().getUsername() : "Chưa có");

            if (lblItemType != null && lblSpecificSpecs != null) {
                String className = auction.getItem().getClass().getSimpleName();
                lblItemType.setText(className);

                switch (className) {
                    case "Electronics": lblSpecificSpecs.setText("Thời hạn bảo hành kỹ thuật số toàn quốc."); break;
                    case "Art": lblSpecificSpecs.setText("Chứng nhận tác giả gốc & Năm sáng tác độc bản."); break;
                    case "Vehicle": lblSpecificSpecs.setText("Số khung máy nguyên bản & Giấy tờ đăng ký chính chủ."); break;
                    default: lblSpecificSpecs.setText("Thông tin tài sản tiêu chuẩn."); break;
                }
            }
        } else {
            lblDetailName.setText("Chưa chọn sản phẩm");
            lblDetailDesc.setText("...");
            lblStartPrice.setText("...");
            lblHighestBidder.setText("...");
            if (lblItemType != null) lblItemType.setText("...");
            if (lblSpecificSpecs != null) lblSpecificSpecs.setText("...");
        }
    }

    private void updateChartDisplay(Auction auction) {
        if (auction == null) {
            activeSeries.getData().clear();
            return;
        }

        List<XYChart.Data<String, Number>> rawPoints = chartRawDataMap.get(auction.getItem().getId());
        if (rawPoints != null) {
            activeSeries.setName(auction.getItem().getName());
            activeSeries.getData().setAll(rawPoints);
        } else {
            activeSeries.getData().clear();
        }
    }

    @FXML
    private void handleBid() {
        if (selectedAuction == null) return;
        String priceInput = txtBidPrice.getText().trim();
        if (priceInput.isEmpty()) return;

        try {
            double bidAmount = Double.parseDouble(priceInput);
            if (bidAmount <= selectedAuction.getItem().getCurrentHighestPrice()) return;

            boolean success = networkClient.sendPlaceBidRequest(selectedAuction.getItem().getId(), currentBidder.getUsername(), bidAmount);
            if (success) {
                selectedAuction.getItem().setCurrentHighestPrice(bidAmount);
                recordNewBidToChart(selectedAuction.getItem().getId(), bidAmount);
                itemTable.refresh();
                updateDetailPanel(selectedAuction);
                txtBidPrice.clear();
            }
        } catch (NumberFormatException ignored) {}
    }

    @FXML
    private void handleAutoBid() {
        if (selectedAuction == null) return;
        String maxBidStr = txtMaxBid.getText().trim();
        String incrementStr = txtIncrement.getText().trim();
        if (maxBidStr.isEmpty() || incrementStr.isEmpty()) return;

        try {
            double maxBid = Double.parseDouble(maxBidStr);
            double increment = Double.parseDouble(incrementStr);
            networkClient.sendRegisterAutoBidRequest(selectedAuction.getItem().getId(), currentBidder.getUsername(), maxBid, increment);
            txtMaxBid.clear();
            txtIncrement.clear();
        } catch (NumberFormatException ignored) {}
    }

    private void recordNewBidToChart(String auctionId, double newPrice) {
        List<XYChart.Data<String, Number>> rawPoints = chartRawDataMap.get(auctionId);
        if (rawPoints != null) {
            String timestamp = LocalTime.now().format(timeFormatter);
            XYChart.Data<String, Number> newPoint = new XYChart.Data<>(timestamp, newPrice);
            rawPoints.add(newPoint);

            Platform.runLater(() -> {
                if (selectedAuction != null && selectedAuction.getItem().getId().equals(auctionId)) {
                    activeSeries.getData().add(newPoint);
                }
            });
        }
    }

    @FXML private void handleRefresh() { loadAuctions(); }
    @FXML private void handleGoToHistory() {}

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/AuctionSystem/view/Login.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.show();
        } catch (Exception ignored) {}
    }

    @Override
    public void onAuctionUpdated(Auction auction) {
        Platform.runLater(() -> {
            if (auction == null || auction.getItem() == null) return;
            recordNewBidToChart(auction.getItem().getId(), auction.getItem().getCurrentHighestPrice());

            for (int i = 0; i < auctionList.size(); i++) {
                if (auctionList.get(i).getItem().getId().equals(auction.getItem().getId())) {
                    auctionList.set(i, auction);
                    break;
                }
            }
            itemTable.refresh();

            if (selectedAuction != null && selectedAuction.getItem().getId().equals(auction.getItem().getId())) {
                selectedAuction = auction;
                updateDetailPanel(selectedAuction);
            }
        });
    }
}