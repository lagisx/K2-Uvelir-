package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.CartManager;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

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
    @FXML private Label imageCounter;

    private JSONObject product;
    private List<String> imageUrls = new ArrayList<>();
    private int currentIndex = 0;
    private MainController mainController;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    public void setProduct(JSONObject product) {
        this.product = product;

        nameLabel.setText(product.optString("name", "Без имени"));
        materialLabel.setText(product.optString("material", "-"));
        priceLabel.setText(String.format("%.2f ₽", product.optDouble("price", 0)));
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
            placeholder.setFitWidth(450);
            placeholder.setFitHeight(450);
            imageContainer.getChildren().add(placeholder);
            updateImageCounter();
        } else {
            showImage(currentIndex);
        }
    }

    private void showImage(int index) {
        imageContainer.getChildren().clear();
        
        ImageView imageView = new ImageView(new Image(imageUrls.get(index), 450, 450, true, true));
        imageView.setPreserveRatio(true);
        
        imageContainer.getChildren().add(imageView);
        updateImageCounter();
    }

    private void updateImageCounter() {
        if (imageCounter != null) {
            int total = imageUrls.isEmpty() ? 0 : imageUrls.size();
            int current = imageUrls.isEmpty() ? 0 : currentIndex + 1;
            imageCounter.setText(current + " / " + total);
        }
        
        if (prevButton != null) {
            prevButton.setDisable(imageUrls.isEmpty() || currentIndex == 0);
        }
        if (nextButton != null) {
            nextButton.setDisable(imageUrls.isEmpty() || currentIndex == imageUrls.size() - 1);
        }
    }

    @FXML
    private void prevImage() {
        if (!imageUrls.isEmpty() && currentIndex > 0) {
            currentIndex--;
            showImage(currentIndex);
        }
    }

    @FXML
    private void nextImage() {
        if (!imageUrls.isEmpty() && currentIndex < imageUrls.size() - 1) {
            currentIndex++;
            showImage(currentIndex);
        }
    }

    @FXML
    private void addToCart() {
        if (product != null) {
            CartManager cartManager = CartManager.getInstance();
            String imageUrl = imageUrls.isEmpty() ? "" : imageUrls.get(0);
            cartManager.addProduct(product, imageUrl);
            
            showSuccess();
        }
    }

    private void showSuccess() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Успех");
        alert.setHeaderText(null);
        alert.setContentText("Товар добавлен в корзину!");
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            
            MainController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getUser());
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
