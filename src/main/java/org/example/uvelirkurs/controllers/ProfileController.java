package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONObject;

public class ProfileController {

    @FXML private TextField usernameField;
    @FXML private Label statusLabel;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField currentPasswordField;
    @FXML private Label userNameDisplay;
    @FXML private Label avatarLabel;

    private JSONObject currentUser;

    public void setCurrentUser(JSONObject user) {
        this.currentUser = user;
        usernameField.setText(user.optString("username"));
        nameField.setText(user.optString("fullname"));
        emailField.setText(user.optString("email"));
        phoneField.setText(user.optString("phone"));
        
        String displayName = user.optString("fullname", user.optString("username", "Пользователь"));
        if (userNameDisplay != null) {
            userNameDisplay.setText(displayName);
        }
        
        if (avatarLabel != null && !displayName.isEmpty()) {
            avatarLabel.setText(displayName.substring(0, 1).toUpperCase());
        }
    }

    @FXML
    private void saveProfile() {
        if (currentUser == null) return;

        String currentPassword = currentPasswordField.getText().trim();
        if (currentPassword.isEmpty() || !currentPassword.equals(currentUser.optString("password"))) {
            showStatus("Введите правильный текущий пароль", "error");
            return;
        }

        String usernameToUpdate = usernameField.getText().trim().isEmpty() ? null : usernameField.getText().trim();
        String fullnameToUpdate = nameField.getText().trim().isEmpty() ? null : nameField.getText().trim();
        String emailToUpdate = emailField.getText().trim().isEmpty() ? null : emailField.getText().trim();
        String phoneToUpdate = phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim();
        String passwordToUpdate = passwordField.getText().isEmpty() ? null : passwordField.getText();

        if ((usernameToUpdate == null || usernameToUpdate.equals(currentUser.optString("username"))) &&
                (fullnameToUpdate == null || fullnameToUpdate.equals(currentUser.optString("fullname"))) &&
                (emailToUpdate == null || emailToUpdate.equals(currentUser.optString("email"))) &&
                (phoneToUpdate == null || phoneToUpdate.equals(currentUser.optString("phone"))) &&
                (passwordToUpdate == null)) {
            showStatus("Нет изменений для сохранения", "warning");
            return;
        }

        int userId = currentUser.getInt("id");

        SupabaseService.updateUser(userId, usernameToUpdate, fullnameToUpdate, emailToUpdate, phoneToUpdate, passwordToUpdate)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        if (usernameToUpdate != null) currentUser.put("username", usernameToUpdate);
                        if (fullnameToUpdate != null) currentUser.put("fullname", fullnameToUpdate);
                        if (emailToUpdate != null) currentUser.put("email", emailToUpdate);
                        if (phoneToUpdate != null) currentUser.put("phone", phoneToUpdate);
                        if (passwordToUpdate != null) currentUser.put("password", passwordToUpdate);

                        SessionManager.setUser(currentUser);

                        passwordField.clear();
                        currentPasswordField.clear();

                        showStatus("Данные успешно сохранены", "success");
                        
                        String displayName = currentUser.optString("fullname", currentUser.optString("username", "Пользователь"));
                        if (userNameDisplay != null) {
                            userNameDisplay.setText(displayName);
                        }
                    } else {
                        showStatus("Ошибка при обновлении профиля", "error");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showStatus("Ошибка при обновлении профиля", "error"));
                    ex.printStackTrace();
                    return null;
                });
    }

    @FXML
    private void goBack() {
        if (currentUser == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());

            MainController mainController = loader.getController();
            mainController.setCurrentUser(currentUser);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStatus(String text, String type) {
        statusLabel.setText(text);
        statusLabel.setVisible(true);
    }
}
