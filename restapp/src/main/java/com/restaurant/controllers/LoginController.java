package com.restaurant.controllers;

import com.restaurant.database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblError;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Хэрэглэгчийн нэр эсвэл нууц үгээ оруулна уу!");
            return;
        }

        // Өгөгдлийн сан руу хандаж шалгах
        String query = "SELECT * FROM cashier WHERE username = ? AND password = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Энд энгийн эсвэл шифрлэгдсэн нууц үг шалгана
            
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Нэвтрэх амжилттай болсон үед Кассын дэлгэц рүү шилжих
                Parent cashierRoot = FXMLLoader.load(getClass().getResource("/com/restaurant/cashier.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(cashierRoot);
                stage.setScene(scene);
                stage.setTitle("Рестораны Кассын Систем");
                stage.setMaximized(true);
                stage.centerOnScreen();
            } else {
                lblError.setText("Нэвтрэх нэр эсвэл нууц үг буруу байна!");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            lblError.setText("Өгөгдлийн сантай холбогдоход алдаа гарлаа!");
        }
    }
}