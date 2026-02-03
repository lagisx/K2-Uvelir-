package org.example.uvelirkurs.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

public class ProductDetailsOverlay {

    public static void show(JSONObject product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ProductDetailsOverlay.class.getResource(
                            "/org/example/uvelirkurs/product_details.fxml"
                    )
            );

            StackPane overlay = loader.load();
            ProductDetailsController controller = loader.getController();
            controller.setProduct(product);
            controller.setRoot(overlay);

            Stage stage = (Stage) Stage.getWindows().filtered(w -> w.isShowing()).get(0);
            ((StackPane) stage.getScene().getRoot()).getChildren().add(overlay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
