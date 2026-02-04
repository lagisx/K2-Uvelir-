package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProductCardController {

    @FXML private Label nameLabel;
    @FXML private Label materialLabel;
    @FXML private Label priceLabel;
    @FXML private ImageView imageView;

    private JSONObject product;

    public void setProduct(JSONObject product) {
        this.product = product;

        nameLabel.setText(product.optString("name"));
        materialLabel.setText(product.optString("material"));
        priceLabel.setText(product.optDouble("price") + " ₽");

        JSONArray images = SupabaseService.getProductImages(product.getInt("id"));
        if (!images.isEmpty()) {
            String url = images.getJSONObject(0).getString("image_url");
            imageView.setImage(new Image(url, true));
        } else {
            imageView.setStyle("-fx-background-color:#e5e7eb; -fx-background-radius:10;");
        }
    }

    @FXML
    private void onDetails() {
        ProductDetailsOverlay.show(product);
    }
}
