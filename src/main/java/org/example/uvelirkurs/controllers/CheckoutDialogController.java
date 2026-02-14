package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CheckoutDialogController {

    @FXML private TextField shippingAddressField;
    @FXML private TextField shippingCityField;
    @FXML private TextField shippingPhoneField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextArea notesArea;
    @FXML private Label totalAmountLabel;

    private double totalAmount;
    private CartController cartController;

    @FXML
    public void initialize() {
        paymentMethodCombo.getItems().addAll(
            "Наличные при получении",
            "Банковская карта",
            "Онлайн оплата",
            "Банковский перевод"
        );
        paymentMethodCombo.getSelectionModel().selectFirst();
    }

    public void setTotalAmount(double amount) {
        this.totalAmount = amount;
        totalAmountLabel.setText(String.format("%.2f ₽", amount));
    }

    public void setCartController(CartController controller) {
        this.cartController = controller;
    }

    @FXML
    private void confirmOrder() {
        String address = shippingAddressField.getText().trim();
        String city = shippingCityField.getText().trim();
        String phone = shippingPhoneField.getText().trim();
        String paymentMethod = paymentMethodCombo.getValue();
        String notes = notesArea.getText().trim();

        if (city.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            showError("Пожалуйста, заполните все обязательные поля");
            return;
        }

        if (!phone.matches("^[+]?[0-9\\s()\\-]+$")) {
            showError("Пожалуйста, введите корректный номер телефона");
            return;
        }

        if (cartController != null) {
            cartController.completeOrder(address, city, phone, paymentMethod, notes);
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/cart.fxml"));
            
            Stage stage = (Stage) shippingAddressField.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            
            CartController controller = loader.getController();
            controller.setMainController(null);
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Ошибка");
        alert.setHeaderText("Проверьте введенные данные");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
