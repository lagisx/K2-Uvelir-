package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class MainController {

    @FXML private FlowPane productsPane;

    @FXML private VBox categoriesBox;

    @FXML private Button profileButton;

    @FXML
    public void initialize() {
        loadProductsFromSupabase();
        loadCategories();
        profileButton.setOnAction(e -> {
            try {
                openProfile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    public void setCurrentUser(JSONObject user) {
        SessionManager.setUser(user);
    }
    private void loadProductsFromSupabase() {
        JSONArray products = SupabaseService.getProducts();
        renderProducts(products);
    }

    private void loadCategories() {
        JSONArray categories = SupabaseService.getCategories();

        categoriesBox.getChildren().clear();

        Button allBtn = new Button("Все товары");
        allBtn.setMaxWidth(Double.MAX_VALUE);
        allBtn.setOnAction(e -> loadProductsFromSupabase());
        categoriesBox.getChildren().add(allBtn);

        for (int i = 0; i < categories.length(); i++) {
            JSONObject cat = categories.getJSONObject(i);

            Button btn = new Button(cat.getString("name"));
            btn.setMaxWidth(Double.MAX_VALUE);

            int categoryId = cat.getInt("id");
            btn.setOnAction(e -> loadProductsByCategory(categoryId));

            categoriesBox.getChildren().add(btn);
        }

    }
    private void loadProductsByCategory(int categoryId) {
        JSONArray products = SupabaseService.getProductsByCategory(categoryId);
        renderProducts(products);
    }
    private void renderProducts(JSONArray products) {
        productsPane.getChildren().clear();

        for (int i = 0; i < products.length(); i++) {
            addProductCard(products.getJSONObject(i));
        }
    }

    public void openProfile() throws IOException {
        JSONObject user = SessionManager.getUser();
        if (user == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/profile.fxml"));
        Scene scene = new Scene(loader.load());
        ProfileController controller = loader.getController();
        controller.setCurrentUser(user);

        Stage stage = (Stage) profileButton.getScene().getWindow();
        stage.setScene(scene);
    }



    private void addProductCard(JSONObject product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/uvelirkurs/product_card.fxml")
            );

            VBox card = loader.load();
            ProductCardController controller = loader.getController();
            controller.setProduct(product);

            productsPane.getChildren().add(card);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
