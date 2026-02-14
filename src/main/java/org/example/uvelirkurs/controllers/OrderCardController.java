package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

public class OrderCardController {

    @FXML private Label orderNumberLabel;
    @FXML private Label dateLabel;
    @FXML private VBox detailsContainer;
    @FXML private Label totalAmountLabel;
    @FXML private VBox orderItemsContainer;

    private JSONObject order;

    public void setOrder(JSONObject order) {
        this.order = order;
        updateUI();
        loadOrderItems();
    }

    private void updateUI() {
        if (order == null) return;

        orderNumberLabel.setText("Заказ #" + order.getString("order_number"));
        dateLabel.setText(formatDate(order.getString("created_at")));

        detailsContainer.getChildren().clear();
        detailsContainer.getChildren().add(createDetailRow("Адрес",
            order.getString("shipping_city") + ", " + order.getString("shipping_address")));
        detailsContainer.getChildren().add(createDetailRow("Телефон",
            order.getString("shipping_phone")));
        detailsContainer.getChildren().add(createDetailRow("Оплата",
            order.getString("payment_method")));

        if (order.has("notes") && !order.isNull("notes") && !order.getString("notes").isEmpty()) {
            detailsContainer.getChildren().add(createDetailRow("Примечания",
                order.getString("notes")));
        }

        totalAmountLabel.setText(String.format("%.2f ₽", order.getDouble("total_amount")));
    }

    private void loadOrderItems() {
        if (order == null || orderItemsContainer == null) return;

        int orderId = order.getInt("id");

        new Thread(() -> {
            JSONArray orderItems = SupabaseService.getOrderItems(orderId);

            Platform.runLater(() -> {
                orderItemsContainer.getChildren().clear();

                if (orderItems.isEmpty()) {
                    Label emptyLabel = new Label("Нет товаров в заказе");
                    emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px; -fx-padding: 10;");
                    orderItemsContainer.getChildren().add(emptyLabel);
                    return;
                }

                for (int i = 0; i < orderItems.length(); i++) {
                    JSONObject item = orderItems.getJSONObject(i);
                    int productId = item.getInt("product_id");
                    int quantity = item.getInt("quantity");
                    double price = item.getDouble("price");

                    JSONObject product = SupabaseService.getProductById(productId);

                    VBox itemBox = createOrderItemRow(product, quantity, price);
                    orderItemsContainer.getChildren().add(itemBox);
                }
            });
        }).start();
    }

    private VBox createOrderItemRow(JSONObject product, int quantity, double price) {
        VBox itemBox = new VBox(6);
        itemBox.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 6;");

        HBox mainInfo = new HBox(10);
        mainInfo.setStyle("-fx-alignment: CENTER_LEFT;");

        Label nameLabel = new Label(product != null ? product.optString("name", "Неизвестный товар") : "Товар удален");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label quantityLabel = new Label("× " + quantity);
        quantityLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label priceLabel = new Label(String.format("%.2f ₽", price));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4a5fd8;");

        mainInfo.getChildren().addAll(nameLabel, spacer, quantityLabel, priceLabel);

        if (product != null) {
            HBox detailsInfo = new HBox(15);
            detailsInfo.setStyle("-fx-padding: 5 0 0 0;");

            String material = product.optString("material", "");
            if (!material.isEmpty()) {
                Label materialLabel = new Label("Материал: " + material);
                materialLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
                detailsInfo.getChildren().add(materialLabel);
            }

            double totalItemPrice = price * quantity;
            Label totalLabel = new Label(String.format("Итого: %.2f ₽", totalItemPrice));
            totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-weight: bold;");

            Region detailSpacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(detailSpacer, javafx.scene.layout.Priority.ALWAYS);

            detailsInfo.getChildren().addAll(detailSpacer, totalLabel);
            itemBox.getChildren().addAll(mainInfo, detailsInfo);
        } else {
            itemBox.getChildren().add(mainInfo);
        }

        return itemBox;
    }

    private VBox createDetailRow(String label, String value) {
        VBox row = new VBox(4);
        row.setStyle("-fx-padding: 8 0;");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888; -fx-font-weight: bold;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        valueText.setWrapText(true);

        row.getChildren().addAll(labelText, valueText);
        return row;
    }

    private String formatDate(String dateString) {
        try {
            String[] parts = dateString.split("T");
            if (parts.length > 0) {
                String[] dateParts = parts[0].split("-");
                if (dateParts.length == 3) {
                    return dateParts[2] + "." + dateParts[1] + "." + dateParts[0];
                }
            }
            return dateString.substring(0, 10);
        } catch (Exception e) {
            return dateString;
        }
    }
}
