package com.example.logincontroller.controller;

import com.example.logincontroller.model.Item;
import com.example.logincontroller.model.ItemFactory;
import com.example.logincontroller.network.ItemDTO;
import com.example.logincontroller.network.NetworkClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;

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
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu sản phẩm: " + newItem.getName());
                clearForm();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
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
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật sản phẩm.");
                clearForm();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        }
    }

    @FXML
    public void handleDeleteItem(ActionEvent event) {
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng chọn sản phẩm để xóa.");
            return;
        }
        if (networkClient.sendDeleteItemRequest(selectedItem.getId())) {
            itemList.remove(selectedItem);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa sản phẩm.");
            clearForm();
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
}