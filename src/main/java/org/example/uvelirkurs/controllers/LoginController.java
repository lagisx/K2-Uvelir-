package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONObject;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Пожалуйста, заполните все поля");
            return;
        }

        boolean success = SupabaseService.loginUser(email, password);
        if (success) {
            JSONObject user = SupabaseService.getCurrentUserByEmail(email);
            if (user != null) {
                SessionManager.login(user);

                String role = user.optString("role", "CLIENT");

                Stage stage = (Stage) emailField.getScene().getWindow();

                if ("ADMIN".equals(role)) {
                    openAdminPanel(stage);
                } else if ("MANAGER".equals(role)) {
                    openManagerPanel(stage);
                } else {
                    openMainMenu(stage, user);
                }
            } else {
                showError("Не удалось получить данные пользователя");
            }
        } else {
            showError("Неверный email или пароль");
        }
    }


    private void openAdminPanel(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/admin_panel.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setTitle("Uvelir Shop - Панель Администратора");
    }


    private void openManagerPanel(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/manager_panel.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setTitle("Uvelir Shop - Панель Менеджера");
    }


    private void openMainMenu(Stage stage, JSONObject user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
        Scene scene = new Scene(loader.load());

        MainController mainController = loader.getController();
        mainController.setCurrentUser(user);

        stage.setScene(scene);
        stage.setTitle("Uvelir Shop - Магазин");
    }

    @FXML
    private void goToRegister() throws Exception {
        Stage stage = (Stage) emailField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/register.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setMaximized(true);
        stage.setScene(scene);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
