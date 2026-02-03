package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainController {

    @FXML
    private FlowPane productsPane;

    @FXML
    public void initialize() {
        loadProductsFromSupabase();
    }

    private void loadProductsFromSupabase() {
        JSONArray products = SupabaseService.getProducts();

        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);
            addProductCard(product);
        }
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
