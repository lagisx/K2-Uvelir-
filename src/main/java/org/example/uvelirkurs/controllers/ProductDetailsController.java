package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import org.json.JSONObject;

public class ProductDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label materialLabel;
    @FXML private Label priceLabel;
    @FXML private Label descriptionLabel;

    private StackPane root;

    public void setRoot(StackPane root) {
        this.root = root;
    }

    public void setProduct(JSONObject product) {
        nameLabel.setText(product.optString("name", "Без имени"));
        materialLabel.setText(product.optString("material", "-"));
        priceLabel.setText(product.optDouble("price", 0) + " ₽");
        descriptionLabel.setText(product.optString("description", "Нет описания"));
    }

    @FXML
    private void close() {
        if (root != null) {
            ((StackPane) root.getParent()).getChildren().remove(root);
        }
    }
}
