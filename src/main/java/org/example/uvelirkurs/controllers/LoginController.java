package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SupabaseService;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextFlow statusFlow;

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Введите email и пароль");
            return;
        }

        boolean success = SupabaseService.loginUser(email, password);
        if (success) {
            Stage stage = (Stage) emailField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml")
            );
            stage.setScene(new Scene(loader.load()));
        } else {
            showStatus("Неверный email или пароль");
        }
    }

    @FXML
    private void goToRegister() throws Exception {
        Stage stage = (Stage) emailField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/register.fxml"));
        stage.setScene(new Scene(loader.load()));
    }

    private void showStatus(String message) {
        statusFlow.getChildren().clear();
        Text text = new Text(message);
        text.setStyle("-fx-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
        statusFlow.getChildren().add(text);
    }
}
