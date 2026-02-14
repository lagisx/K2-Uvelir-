package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONObject;

public class OrderHistoryController {

    @FXML private VBox ordersContainer;
    @FXML private VBox emptyOrdersContainer;
    @FXML private Label emptyLabel;

    private MainController mainController;

    @FXML
    public void initialize() {
        loadUserOrders();
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    private void loadUserOrders() {
        JSONObject currentUser = SessionManager.getUser();
        if (currentUser == null) {
            emptyLabel.setText("Пользователь не авторизован");
            emptyOrdersContainer.setVisible(true);
            ordersContainer.setVisible(false);
            return;
        }

        int userId = currentUser.getInt("id");
        
        SupabaseService.getUserOrdersAsync(userId).thenAccept(orders -> {
            Platform.runLater(() -> {
                if (orders.isEmpty()) {
                    emptyOrdersContainer.setVisible(true);
                    ordersContainer.setVisible(false);
                    return;
                }

                emptyOrdersContainer.setVisible(false);
                ordersContainer.setVisible(true);
                ordersContainer.getChildren().clear();

                for (int i = 0; i < orders.length(); i++) {
                    JSONObject order = orders.getJSONObject(i);
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/order_card.fxml"));
                        VBox orderCard = loader.load();
                        
                        OrderCardController controller = loader.getController();
                        controller.setOrder(order);
                        
                        ordersContainer.getChildren().add(orderCard);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                emptyLabel.setText("Ошибка загрузки заказов");
                emptyOrdersContainer.setVisible(true);
                ordersContainer.setVisible(false);
            });
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
            
            Stage stage = (Stage) ordersContainer.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            
            MainController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getUser());
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
