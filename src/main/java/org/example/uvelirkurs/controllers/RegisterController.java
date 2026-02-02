package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SupabaseService;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullnameField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel;

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String fullname = fullnameField.getText().trim();
        String phone = phoneField.getText().trim();

        /* if (email.isEmpty() || password.isEmpty() || fullname.isEmpty()) {
            showStatus("Заполните все обязательные поля");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showStatus("Введите корректный email");
            return;
        }
         */

        boolean success = SupabaseService.registerUser(email, password, fullname, phone);

        if (success) {
            showStatus("Регистрация успешна! Данные сохранены в базе.");
        } else {
            showStatus("Ошибка записи в базу (пользователь может уже существовать)");
        }
    }

    @FXML
    private void goToLogin() throws Exception {
        Stage stage = (Stage) emailField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/login.fxml"));
        stage.setScene(new Scene(loader.load()));
    }

    private void showStatus(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
