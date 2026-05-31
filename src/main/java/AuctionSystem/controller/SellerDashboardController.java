package AuctionSystem.controller;

import AuctionSystem.model.Item;
import AuctionSystem.model.ItemFactory;
import AuctionSystem.network.ItemDTO;
import AuctionSystem.network.NetworkClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.util.List;

public class SellerDashboardController {
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, Double> colPrice;
    @FXML private TableColumn<Item, Double> colHighestPrice;
    @FXML private TableColumn<Item, LocalDateTime> colStartTime;
    @FXML private TableColumn<Item, LocalDateTime> colEndTime;

    @FXML private TextField txtStartTime, txtEndTime;
    @FXML private TextField txtName, txtStartingPrice, txtExtraInfo;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbItemType;
    @FXML private DatePicker dpStartTime, dpEndTime;

    private final ObservableList<Item> itemList = FXCollections.observableArrayList();
    private final NetworkClient networkClient = new NetworkClient();
    private Item selectedItem = null;
    private final java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        cbItemType.setItems(FXCollections.observableArrayList("Sản phẩm điện tử", "Tác phẩm nghệ thuật", "Phương tiện di chuyển"));

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colHighestPrice.setCellValueFactory(new PropertyValueFactory<>("currentHighestPrice"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        itemTable.setItems(itemList);

        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        colStartTime.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateTimeFormatter.format(date));
                }
            }
        });

        colEndTime.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateTimeFormatter.format(date));
                }
            }
        });

        java.text.NumberFormat formatVN = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(formatVN.format(price) + " VNĐ");
                }
            }
        });

        colHighestPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(formatVN.format(price) + " VNĐ");
                }
            }
        });

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedItem = newSelection;
                fillFormWithItem(newSelection);
            }
        });
        try {
            List<Item> itemsOnServer = networkClient.sendGetAllItemsRequest();
            if (itemsOnServer != null && !itemsOnServer.isEmpty()) {
                itemList.clear();
                itemList.addAll(itemsOnServer);
                itemTable.refresh();
            }
        } catch (Exception e) {
            System.err.println("Không thể tải sản phẩm ban đầu từ Server: " + e.getMessage());
        }

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedItem = newSelection;
                fillFormWithItem(newSelection);
            }
        });
    }

    @FXML
    public void handleAddItem(ActionEvent event) {
        if (!validateInput()) return;
        try {
            LocalDateTime startDateTime = LocalDateTime.of(dpStartTime.getValue(), java.time.LocalTime.parse(txtStartTime.getText(), timeFormatter));
            LocalDateTime endDateTime = LocalDateTime.of(dpEndTime.getValue(), java.time.LocalTime.parse(txtEndTime.getText(), timeFormatter));
            Item newItem = ItemFactory.createItem(
                    cbItemType.getValue(), "ID_" + System.currentTimeMillis(), txtName.getText(),
                    txtDescription.getText(), Double.parseDouble(txtStartingPrice.getText()),
                    startDateTime, endDateTime, txtExtraInfo.getText()
            );

            ItemDTO dto = createDTOFromForm(newItem.getId());
            if (networkClient.sendAddItemRequest(dto)) {
                itemList.add(newItem);

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm sản phẩm thành công: " + newItem.getName());
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm sản phẩm không thành công (Server từ chối).");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi khi thêm sản phẩm", e.getMessage());
        }
    }

    @FXML
    public void handleUpdateItem(ActionEvent event) {
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng chọn sản phẩm trong bảng để cập nhật.");
            return;
        }
        if (!validateInput()) return;

        try {
            LocalDateTime startDateTime = LocalDateTime.of(dpStartTime.getValue(), java.time.LocalTime.parse(txtStartTime.getText(), timeFormatter));
            LocalDateTime endDateTime = LocalDateTime.of(dpEndTime.getValue(), java.time.LocalTime.parse(txtEndTime.getText(), timeFormatter));

            ItemDTO dto = createDTOFromForm(selectedItem.getId());
            if (networkClient.sendUpdateItemRequest(dto)) {
                selectedItem.setName(txtName.getText());
                selectedItem.setStartingPrice(Double.parseDouble(txtStartingPrice.getText()));
                selectedItem.setStartTime(startDateTime);
                selectedItem.setEndTime(endDateTime);
                itemTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật sản phẩm thành công.");
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật sản phẩm không thành công (Server từ chối).");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi khi cập nhật sản phẩm", e.getMessage());
        }
    }

    @FXML
    public void handleDeleteItem(ActionEvent event) {
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng chọn sản phẩm để xóa.");
            return;
        }
        try {
            if (networkClient.sendDeleteItemRequest(selectedItem.getId())) {
                itemList.remove(selectedItem);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa sản phẩm thành công.");
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa sản phẩm không thành công (Server từ chối).");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi khi xóa sản phẩm", e.getMessage());
        }
    }

    private boolean validateInput() {
        if (txtName.getText().isEmpty() || cbItemType.getValue() == null || dpStartTime.getValue() == null || dpEndTime.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Vui lòng nhập đầy đủ thông tin bắt buộc.");
            return false;
        }
        try {
            double price = Double.parseDouble(txtStartingPrice.getText());
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Lỗi định dạng", "Giá khởi điểm phải là một số lớn hơn 0.");
            return false;
        }
        if (dpEndTime.getValue().isBefore(dpStartTime.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Lỗi thời gian", "Ngày kết thúc không được diễn ra trước ngày bắt đầu.");
            return false;
        }
        if (txtStartTime.getText().isEmpty() || txtEndTime.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Vui lòng nhập thời gian bắt đầu và kết thúc.");
            return false;
        }

        try {
            java.time.LocalTime.parse(txtStartTime.getText(), timeFormatter);
            java.time.LocalTime.parse(txtEndTime.getText(), timeFormatter);
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Lỗi định dạng giờ", "Vui lòng nhập giờ theo định dạng Giờ:Phút:Giây (Ví dụ: 08:30:00, 14:00:59).");
            return false;
        }

        LocalDateTime startDateTime = LocalDateTime.of(dpStartTime.getValue(), java.time.LocalTime.parse(txtStartTime.getText(), timeFormatter));
        LocalDateTime endDateTime = LocalDateTime.of(dpEndTime.getValue(), java.time.LocalTime.parse(txtEndTime.getText(), timeFormatter));

        if (endDateTime.isBefore(startDateTime)) {
            showAlert(Alert.AlertType.WARNING, "Lỗi thời gian", "Thời gian kết thúc không được diễn ra trước thời gian bắt đầu.");
            return false;
        }

        if (endDateTime.isBefore(LocalDateTime.now())) {
            showAlert(Alert.AlertType.WARNING, "Lỗi thời gian",
                    "Thời gian kết thúc đã qua rồi!\n"
                    + "Ngày kết thúc bạn chọn: " + endDateTime + "\n"
                    + "Hiện tại: " + LocalDateTime.now().withNano(0) + "\n\n"
                    + "Vui lòng chọn thời gian kết thúc trong tương lai.");
            return false;
        }
        return true;
    }

    private ItemDTO createDTOFromForm(String id) {
        LocalDateTime start = LocalDateTime.of(dpStartTime.getValue(), java.time.LocalTime.parse(txtStartTime.getText(), timeFormatter));
        LocalDateTime end = LocalDateTime.of(dpEndTime.getValue(), java.time.LocalTime.parse(txtEndTime.getText(), timeFormatter));

        return new ItemDTO(id, txtName.getText(), txtDescription.getText(),
                Double.parseDouble(txtStartingPrice.getText()),
                start, end,
                cbItemType.getValue(), txtExtraInfo.getText());
    }

    private void fillFormWithItem(Item item) {
        txtName.setText(item.getName());
        txtDescription.setText(item.getDescription());
        txtStartingPrice.setText(String.format("%.0f", item.getStartingPrice()));

        dpStartTime.setValue(item.getStartTime().toLocalDate());
        txtStartTime.setText(item.getStartTime().toLocalTime().format(timeFormatter));

        dpEndTime.setValue(item.getEndTime().toLocalDate());
        txtEndTime.setText(item.getEndTime().toLocalTime().format(timeFormatter));
    }

    private void clearForm() {
        selectedItem = null;
        txtName.clear(); txtDescription.clear(); txtStartingPrice.clear();
        txtExtraInfo.clear();
        dpStartTime.setValue(null); dpEndTime.setValue(null);
        txtStartTime.clear(); txtEndTime.clear();
        itemTable.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        // 1. Hiện hộp thoại hỏi lại cho chắc chắn
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống Seller?");

        java.util.Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Tải lại giao diện màn hình Đăng nhập (hello-view.fxml)
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/hello-view.fxml"));
                javafx.scene.Parent root = loader.load();

                // Lấy Stage (cửa sổ) hiện tại từ nút bấm vừa click
                javafx.stage.Stage currentStage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

                // Đổi Scene và ĐỔI LẠI TIÊU ĐỀ cửa sổ về màn hình đăng nhập
                currentStage.setScene(new javafx.scene.Scene(root));
                currentStage.setTitle("Đăng nhập hệ thống");

                currentStage.centerOnScreen();
                currentStage.show();

            } catch (java.io.IOException e) {
                System.err.println("Lỗi khi chuyển màn hình đăng xuất: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}