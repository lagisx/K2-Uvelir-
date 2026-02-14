package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.CartManager;
import org.example.uvelirkurs.models.CartItem;
import org.json.JSONObject;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private VBox emptyCartContainer;
    @FXML private Label totalPriceLabel;
    @FXML private Button checkoutButton;

    private CartManager cartManager;
    private MainController mainController;

    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();
        loadCartItems();

        cartManager.getCartItems().addListener((javafx.collections.ListChangeListener<CartItem>) c -> {
            loadCartItems();
        });
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    private void loadCartItems() {
        cartItemsContainer.getChildren().clear();

        if (cartManager.isEmpty()) {
            emptyCartContainer.setVisible(true);
            cartItemsContainer.setVisible(false);
            checkoutButton.setDisable(true);
            totalPriceLabel.setText("0 ₽");
            return;
        }

        emptyCartContainer.setVisible(false);
        cartItemsContainer.setVisible(true);
        checkoutButton.setDisable(false);

        for (CartItem item : cartManager.getCartItems()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/cart_item.fxml"));
                HBox cartItemCard = loader.load();

                CartItemController controller = loader.getController();
                controller.setCartItem(item);

                cartItemsContainer.getChildren().add(cartItemCard);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = cartManager.getTotalPrice();
        totalPriceLabel.setText(String.format("%.2f ₽", total));
    }

    @FXML
    private void handleCheckout() {
        if (cartManager.isEmpty()) {
            showAlert("Ошибка", "Корзина пуста!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/checkout_dialog.fxml"));

            Stage stage = (Stage) cartItemsContainer.getScene().getWindow();
            Scene scene = new Scene(loader.load());

            scene.getRoot().prefWidth(stage.getWidth());
            scene.getRoot().prefHeight(stage.getHeight());

            CheckoutDialogController controller = loader.getController();
            controller.setTotalAmount(cartManager.getTotalPrice());
            controller.setCartController(this);

            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть форму оформления заказа");
        }
    }

    public void completeOrder(String shippingAddress, String shippingCity, String shippingPhone,
                              String paymentMethod, String notes) {
        javafx.application.Platform.runLater(() -> {
            JSONObject currentUser = org.example.uvelirkurs.BDandAPI.SessionManager.getUser();
            if (currentUser == null) {
                showAlert("Ошибка", "Пользователь не авторизован!");
                return;
            }

            int userId = currentUser.getInt("id");
            double totalAmount = cartManager.getTotalPrice();

            int orderId = org.example.uvelirkurs.BDandAPI.SupabaseService.createOrder(
                    userId, totalAmount, shippingAddress, shippingCity,
                    shippingPhone, paymentMethod, notes
            );

            if (orderId == -1) {
                showAlert("Ошибка", "Не удалось создать заказ. Попробуйте еще раз.");
                return;
            }

            boolean allItemsAdded = true;
            for (CartItem item : cartManager.getCartItems()) {
                boolean added = org.example.uvelirkurs.BDandAPI.SupabaseService.addOrderItem(
                        orderId, item.getProductId(), item.getQuantity(), item.getPrice()
                );
                if (!added) {
                    allItemsAdded = false;
                }
            }

            if (allItemsAdded) {
                showAlert("Успех", "Заказ успешно оформлен!\nОбщая сумма: " +
                        String.format("%.2f ₽", totalAmount));
                cartManager.clearCart();
                goBackSafely();
            } else {
                showAlert("Предупреждение", "Заказ создан, но некоторые товары не были добавлены.");
            }
        });
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void clearCart() {
        cartManager.clearCart();
    }

    @FXML
    private void goBack() {
        goBackSafely();
    }

    private void goBackSafely() {
        try {
            if (cartItemsContainer == null || cartItemsContainer.getScene() == null) {
                return;
            }

            Stage stage = (Stage) cartItemsContainer.getScene().getWindow();

            if (stage == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());

            MainController controller = loader.getController();
            controller.setCurrentUser(org.example.uvelirkurs.BDandAPI.SessionManager.getUser());

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
