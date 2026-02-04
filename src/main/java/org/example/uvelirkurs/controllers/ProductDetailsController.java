package org.example.uvelirkurs.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label materialLabel;
    @FXML private Label priceLabel;
    @FXML private Label descriptionLabel;
    @FXML private StackPane imageContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    private StackPane root;
    private JSONObject product;
    private List<String> imageUrls = new ArrayList<>();
    private int currentIndex = 0;

    public void setRoot(StackPane root) {
        this.root = root;
    }

    public void setProduct(JSONObject product) {
        this.product = product;

        nameLabel.setText(product.optString("name", "Без имени"));
        materialLabel.setText(product.optString("material", "-"));
        priceLabel.setText(product.optDouble("price", 0) + " ₽");
        descriptionLabel.setText(product.optString("description", "Нет описания"));

        loadImages(product.getInt("id"));
    }

    private void loadImages(int productId) {
        imageContainer.getChildren().clear();
        imageUrls.clear();
        currentIndex = 0;

        JSONArray images = SupabaseService.getProductImages(productId);
        for (int i = 0; i < images.length(); i++) {
            imageUrls.add(images.getJSONObject(i).getString("image_url"));
        }

        if (imageUrls.isEmpty()) {
            ImageView placeholder = new ImageView();
            placeholder.setFitWidth(350);
            placeholder.setFitHeight(350);
            placeholder.setStyle("-fx-background-color:#e5e7eb; -fx-background-radius:12;");
            imageContainer.getChildren().add(placeholder);
        } else {
            showImage(currentIndex, null);
        }
    }

    private void showImage(int index, ImageView oldImage) {
        ImageView newImage = new ImageView(new Image(imageUrls.get(index), 350, 350, true, true));
        newImage.setStyle("-fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#ddd; -fx-border-width:1;");

        if (oldImage != null) {
            imageContainer.getChildren().add(newImage);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), newImage);
            tt.setFromX(index > currentIndex ? 350 : -350);
            tt.setToX(0);
            tt.play();

            TranslateTransition ttOld = new TranslateTransition(Duration.millis(300), oldImage);
            ttOld.setFromX(0);
            ttOld.setToX(index > currentIndex ? -350 : 350);
            ttOld.setOnFinished(e -> imageContainer.getChildren().remove(oldImage));
            ttOld.play();
        } else {
            imageContainer.getChildren().add(newImage);
        }
    }

    @FXML
    private void prevImage() {
        if (!imageUrls.isEmpty()) {
            int oldIndex = currentIndex;
            currentIndex = (currentIndex - 1 + imageUrls.size()) % imageUrls.size();
            showImage(currentIndex, (ImageView) imageContainer.getChildren().get(0));
        }
    }

    @FXML
    private void nextImage() {
        if (!imageUrls.isEmpty()) {
            int oldIndex = currentIndex;
            currentIndex = (currentIndex + 1) % imageUrls.size();
            showImage(currentIndex, (ImageView) imageContainer.getChildren().get(0));
        }
    }

    @FXML
    private void close() {
        if (root != null) {
            ((StackPane) root.getParent()).getChildren().remove(root);
        }
    }
}
