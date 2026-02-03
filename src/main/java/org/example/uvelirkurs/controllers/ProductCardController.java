package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.json.JSONObject;

public class ProductCardController {

    @FXML private Label nameLabel;
    @FXML private Label materialLabel;
    @FXML private Label priceLabel;

    private JSONObject product;

    public void setProduct(JSONObject product) {
        this.product = product;
        nameLabel.setText(product.optString("name", "Без имени"));
        materialLabel.setText(product.optString("material", "-"));
        priceLabel.setText(product.optDouble("price", 0) + " ₽");
    }

    @FXML
    private void onDetails() {
        ProductDetailsOverlay.show(product);
    }
}
