package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.CartManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProductCardController {

    @FXML private Label nameLabel;
    @FXML private Label materialLabel;
    @FXML private Label priceLabel;
    @FXML private ImageView imageView;

    private JSONObject product;
    private String currentImageUrl = "";

    public void setProduct(JSONObject product) {
        this.product = product;

        nameLabel.setText(product.optString("name"));
        materialLabel.setText(product.optString("material"));
        priceLabel.setText(String.format("%.2f ₽", product.optDouble("price")));

        JSONArray images = SupabaseService.getProductImages(product.getInt("id"));
        if (!images.isEmpty()) {
            currentImageUrl = images.getJSONObject(0).getString("image_url");
            imageView.setImage(new Image(currentImageUrl, true));
        }
    }

    @FXML
    private void onDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/product_details.fxml"));
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            
            ProductDetailsController controller = loader.getController();
            controller.setProduct(product);
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addToCart() {
        CartManager cartManager = CartManager.getInstance();
        cartManager.addProduct(product, currentImageUrl);
    }
}
