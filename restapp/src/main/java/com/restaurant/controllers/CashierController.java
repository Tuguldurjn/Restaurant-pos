package com.restaurant.controllers;

import com.restaurant.database.DBConnection;
import com.restaurant.models.MenuItem;
import com.restaurant.models.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CashierController {

    @FXML
    private TableView<MenuItem> menuTable;
    @FXML
    private TableColumn<MenuItem, String> colItemName;
    @FXML
    private TableColumn<MenuItem, Double> colItemPrice;

    @FXML
    private TableView<OrderItem> orderTable;
    @FXML
    private TableColumn<OrderItem, String> colOrderName;
    @FXML
    private TableColumn<OrderItem, Double> colOrderPrice;
    @FXML
    private TableColumn<OrderItem, Integer> colOrderQty;
    @FXML
    private TableColumn<OrderItem, Double> colOrderSubTotal;

    @FXML
    private Label lblTotalAmount;
    @FXML
    private ComboBox<String> cmbPaymentMethod;
    @FXML
    private TextField txtCashGiven;
    @FXML
    private Label lblChange;

    private ObservableList<MenuItem> menuList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Төлбөрийн хэлбэрүүдийг ComboBox руу нэмэх
        ObservableList<String> paymentMethods = FXCollections.observableArrayList("Бэлэн мөнгө", "Карт", "QR код / Данс");
        cmbPaymentMethod.setItems(paymentMethods);
        cmbPaymentMethod.setValue("Бэлэн мөнгө");

        // Цэсний хүснэгтийн багануудыг холбох
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Захиалгын хүснэгтийн багануудыг холбох
        colOrderName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colOrderPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colOrderQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colOrderSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        orderTable.setItems(orderList);

        // Цэс дэх хоол дээр 2 удаа дарж захиалгад нэмэх үйлдэл
        menuTable.setRowFactory(tv -> {
            TableRow<MenuItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    MenuItem selectedItem = row.getItem();
                    addToOrder(selectedItem);
                }
            });
            return row;
        });

        // Өгсөн мөнгө өөрчлөгдөхөд хариулт мөнгийг автоматаар тооцох
        txtCashGiven.textProperty().addListener((observable, oldValue, newValue) -> calculateChange());

        // Цэсний өгөгдлийг ачаалах
        loadMenuData();
    }

    // Хоолыг сагсанд нэмэх эсвэл тоо ширхэгийг олшруулах
    private void addToOrder(MenuItem item) {
        boolean found = false;
        for (OrderItem orderItem : orderList) {
            if (orderItem.getItemId() == item.getItemId()) {
                // Аль хэдийн байвал тоо ширхэгийг 1-ээр нэмэгдүүлнэ
                orderItem.setQuantity(orderItem.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            // Шинээр нэмэх
            OrderItem newItem = new OrderItem(0, 0, item.getItemId(), item.getName(), item.getPrice(), 1, item.getPrice());
            orderList.add(newItem);
        }

        orderTable.refresh(); // Хүснэгтийн харагдацыг шинэчлэх
        calculateTotal();
    }

    // Нийт дүнг тооцоолох
    private void calculateTotal() {
        double total = 0;
        for (OrderItem item : orderList) {
            total += item.getSubTotal();
        }
        lblTotalAmount.setText(String.format("%.2f ₮", total));
        calculateChange();
    }

    // Хариулт мөнгө бодох
    private void calculateChange() {
        try {
            String totalStr = lblTotalAmount.getText().replace(" ₮", "").trim();
            double total = Double.parseDouble(totalStr);
            
            if (txtCashGiven.getText().isEmpty()) {
                lblChange.setText("0.00 ₮");
                return;
            }

            double cashGiven = Double.parseDouble(txtCashGiven.getText());
            double change = cashGiven - total;

            if (change >= 0) {
                lblChange.setText(String.format("%.2f ₮", change));
            } else {
                lblChange.setText("Мөнгө дутуу байна");
            }
        } catch (NumberFormatException e) {
            lblChange.setText("0.00 ₮");
        }
    }

    private void loadMenuData() {
        menuList.clear();
        String query = "SELECT * FROM menu_item";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("item_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String category = rs.getString("category");

                menuList.add(new MenuItem(id, name, price, category));
            }

            menuTable.setItems(menuList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ХАСАХ товчлуур: Сонгосон хоолыг сагснаас хасах (эсвэл тоо ширхэгийг багасгах)
    @FXML
    private void handleRemoveItem(ActionEvent event) {
        OrderItem selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            if (selectedOrder.getQuantity() > 1) {
                selectedOrder.setQuantity(selectedOrder.getQuantity() - 1);
            } else {
                orderList.remove(selectedOrder);
            }
            orderTable.refresh();
            calculateTotal();
        } else {
            showAlert("Анхааруулга", "Хасах хоолоо сонгоно уу!");
        }
    }

    // ЦЭВЭРЛЭХ товчлуур: Сагсыг бүхэлд нь хоослох
    @FXML
    private void handleClearOrder(ActionEvent event) {
        orderList.clear();
        lblTotalAmount.setText("0.00 ₮");
        txtCashGiven.clear();
        lblChange.setText("0.00 ₮");
    }
    // ТӨЛӨХ & БАРИМТ ХЭВЛЭХ товчлуур: Захиалгыг өгөгдлийн санд бүртгэж баталгаажуулах
    // ТӨЛӨХ & БАРИМТ ХЭВЛЭХ товчлуур: Мөнгөн дүнг хатуу хянаж баталгаажуулах
    @FXML
    private void handlePayAndPrint(ActionEvent event) {
        // 1. Сагс хоосон эсэхийг шалгах
        if (orderList.isEmpty()) {
            showAlert("Алдаа", "Захиалгын сагс хоосон байна!");
            return; // Үйлдлийг шууд зогсооно
        }

        String paymentMethod = cmbPaymentMethod.getValue();
        String totalStr = lblTotalAmount.getText().replace(" ₮", "").trim();
        double totalAmount = Double.parseDouble(totalStr);

        // 2. Хэрэв бэлэн мөнгөөр төлөх бол мөнгөний хүрэлцээг хатуу шалгах
        if ("Бэлэн мөнгө".equals(paymentMethod)) {
            try {
                if (txtCashGiven.getText() == null || txtCashGiven.getText().trim().isEmpty()) {
                    showAlert("Алдаа", "Өгсөн мөнгөний хэмжээг оруулна уу!");
                    return; // Үйлдлийг шууд зогсооно
                }
                
                double cashGiven = Double.parseDouble(txtCashGiven.getText().trim());
                
                // Мөнгө дутуу байвал гүйлгээ хийхийг КАТЕГОРИК ХОРИГЛОХ
                if (cashGiven < totalAmount) {
                    showAlert("Алдаа", "Өгсөн мөнгө хүрэлцэхгүй байна! Төлбөр дутуу тул гүйлгээг бүртгэх боломжгүй.");
                    return; // == МАШ ЧУХАЛ: Энэ return нь гүйлгээ цааш явагдахаас хамгаална ==
                }
            } catch (NumberFormatException e) {
                showAlert("Алдаа", "Өгсөн мөнгөний утга буруу байна! Зөвхөн тоо оруулна уу.");
                return; // Үйлдлийг шууд зогсооно
            }
        }

        // 3. Бүх шалгуур амжилттай давсан үед л өгөгдлийн сан руу бичих
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Гүйлгээг найдвартай болгох

            // Order хүснэгт рүү үндсэн захиалгыг бүртгэх
            String insertOrder = "INSERT INTO `order` (order_date, total_amount, status, cashier_id) VALUES (NOW(), ?, 'Completed', 1)";
            PreparedStatement orderStmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            
            orderStmt.setDouble(1, totalAmount);
            orderStmt.executeUpdate();

            ResultSet rsKeys = orderStmt.getGeneratedKeys();
            int generatedOrderId = 0;
            if (rsKeys.next()) {
                generatedOrderId = rsKeys.getInt(1);
            }

            // Order_item хүснэгт рүү захиалсан хоол тус бүрээр нь хадгалах
            String insertItem = "INSERT INTO order_item (order_id, item_id, quantity, sub_total) VALUES (?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(insertItem);

            for (OrderItem orderItem : orderList) {
                itemStmt.setInt(1, generatedOrderId);
                itemStmt.setInt(2, orderItem.getItemId());
                itemStmt.setInt(3, orderItem.getQuantity());
                itemStmt.setDouble(4, orderItem.getSubTotal());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            conn.commit(); // Өгөгдлийн санд амжилттай хадгалах

            showAlert("Амжилттай", "Төлбөр амжилттай төлөгдөж, захиалга бүртгэгдлээ!");
            handleClearOrder(null); // Сагсыг цэвэрлэх

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Алдаа", "Өгөгдлийн сан руу бичихэд алдаа гарлаа!");
        }
    }

    // Анхааруулга цонх гаргах туслах метод
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}