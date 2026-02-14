package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.uvelirkurs.BDandAPI.CartManager;
import org.example.uvelirkurs.models.CartItem;

public class CartItemController {

    @FXML private ImageView productImage;
    @FXML private Label nameLabel;
    @FXML private Label materialLabel;
    @FXML private Label priceLabel;
    @FXML private Label quantityLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button decreaseBtn;
    @FXML private Button increaseBtn;
    @FXML private Button removeBtn;

    private CartItem cartItem;
    private CartManager cartManager;

    public void setCartItem(CartItem item) {
        this.cartItem = item;
        this.cartManager = CartManager.getInstance();
        updateUI();
    }

    private void updateUI() {
        if (cartItem == null) return;

        nameLabel.setText(cartItem.getName());
        materialLabel.setText(cartItem.getMaterial());
        priceLabel.setText(String.format("%.2f ₽", cartItem.getPrice()));
        quantityLabel.setText(String.valueOf(cartItem.getQuantity()));
        totalPriceLabel.setText(String.format("%.2f ₽", cartItem.getTotalPrice()));

        if (cartItem.getImageUrl() != null && !cartItem.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(cartItem.getImageUrl(), true);
                productImage.setImage(image);
                productImage.setFitWidth(100);
                productImage.setFitHeight(100);
                productImage.setPreserveRatio(true);
            } catch (Exception e) {
                productImage.setFitWidth(100);
                productImage.setFitHeight(100);
            }
        } else {
            productImage.setFitWidth(100);
            productImage.setFitHeight(100);
        }
    }

    @FXML
    private void increaseQuantity() {
        if (cartItem != null) {
            cartManager.updateQuantity(cartItem.getProductId(), cartItem.getQuantity() + 1);
            updateUI();
        }
    }

    @FXML
    private void decreaseQuantity() {
        if (cartItem != null && cartItem.getQuantity() > 1) {
            cartManager.updateQuantity(cartItem.getProductId(), cartItem.getQuantity() - 1);
            updateUI();
        }
    }

    @FXML
    private void removeItem() {
        if (cartItem != null) {
            cartManager.removeProduct(cartItem.getProductId());
        }
    }
}
