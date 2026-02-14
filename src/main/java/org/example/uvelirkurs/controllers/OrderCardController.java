package org.example.uvelirkurs.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

public class OrderCardController {

    @FXML private Label orderNumberLabel;
    @FXML private Label dateLabel;
    @FXML private VBox detailsContainer;
    @FXML private Label totalAmountLabel;

    private JSONObject order;

    public void setOrder(JSONObject order) {
        this.order = order;
        updateUI();
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

    private VBox createDetailRow(String label, String value) {
        VBox row = new VBox(2);
        
        Label labelText = new Label(label);
        Label valueText = new Label(value);
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
