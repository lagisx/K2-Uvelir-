package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.example.uvelirkurs.BDandAPI.SupabaseServiceExtension;
import org.example.uvelirkurs.models.CategoryItem;
import org.example.uvelirkurs.models.SupplierItem;
import org.json.JSONObject;

public class EditProductDialogController {

    @FXML private Label productIdLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<CategoryItem> categoryCombo;
    @FXML private ComboBox<SupplierItem> supplierCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField materialField;
    @FXML private TextField purityField;
    @FXML private TextField weightField;
    @FXML private TextField sizeField;
    @FXML private TextField priceField;
    @FXML private TextField costPriceField;
    @FXML private TextField stockField;
    @FXML private TextField collectionField;

    private DialogPane dialogPane;
    private int productId;

    @FXML
    public void initialize() {
        loadCategories();
        loadSuppliers();
    }

    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        setupValidation();
    }

    public void setProduct(JSONObject product) {
        this.productId = product.optInt("id", -1);
        if (productIdLabel != null) productIdLabel.setText("Товар #" + productId);
        nameField.setText(product.optString("name", ""));
        descriptionArea.setText(product.optString("description", ""));
        materialField.setText(product.optString("material", ""));
        purityField.setText(product.optString("purity", ""));
        weightField.setText(product.has("weight") ? String.valueOf(product.getDouble("weight")) : "");
        sizeField.setText(product.optString("size", ""));
        priceField.setText(product.has("price") ? String.valueOf(product.getDouble("price")) : "");
        costPriceField.setText(product.has("cost_price") ? String.valueOf(product.getDouble("cost_price")) : "");
        stockField.setText(product.has("stock_quantity") ? String.valueOf(product.getInt("stock_quantity")) : "0");
        collectionField.setText(product.optString("collection", ""));

        int catId = product.optInt("category_id", -1);
        int supId = product.optInt("supplier_id", -1);

        if (catId > 0) {
            Platform.runLater(() -> {
                for (CategoryItem item : categoryCombo.getItems()) {
                    if (item.getId() == catId) { categoryCombo.setValue(item); break; }
                }
            });
        }
        if (supId > 0) {
            Platform.runLater(() -> {
                for (SupplierItem item : supplierCombo.getItems()) {
                    if (item.getId() == supId) { supplierCombo.setValue(item); break; }
                }
            });
        }
    }

    private void setupValidation() {
        if (dialogPane == null) return;
        Button saveButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                        .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                        .findFirst().orElse(null));
        if (saveButton != null) {
            saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!validateInput()) event.consume();
            });
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        if (nameField.getText().trim().isEmpty()) errors.append("• Название обязательно\n");
        if (materialField.getText().trim().isEmpty()) errors.append("• Материал обязателен\n");
        if (priceField.getText().trim().isEmpty()) {
            errors.append("• Цена обязательна\n");
        } else {
            try {
                if (Double.parseDouble(priceField.getText().trim()) <= 0) errors.append("• Цена > 0\n");
            } catch (NumberFormatException e) { errors.append("• Цена должна быть числом\n"); }
        }
        if (!weightField.getText().trim().isEmpty()) {
            try { Double.parseDouble(weightField.getText().trim()); }
            catch (NumberFormatException e) { errors.append("• Вес должен быть числом\n"); }
        }
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Исправьте ошибки:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    public JSONObject getProductData() {
        JSONObject product = new JSONObject();
        product.put("name", nameField.getText().trim());
        if (categoryCombo.getValue() != null) product.put("category_id", categoryCombo.getValue().getId());
        if (supplierCombo.getValue() != null) product.put("supplier_id", supplierCombo.getValue().getId());
        product.put("description", descriptionArea.getText().trim());
        product.put("material", materialField.getText().trim());
        product.put("purity", purityField.getText().trim());
        product.put("collection", collectionField.getText().trim());
        if (!priceField.getText().trim().isEmpty())
            product.put("price", Double.parseDouble(priceField.getText().trim()));
        if (!costPriceField.getText().trim().isEmpty())
            product.put("cost_price", Double.parseDouble(costPriceField.getText().trim()));
        if (!stockField.getText().trim().isEmpty())
            product.put("stock_quantity", Integer.parseInt(stockField.getText().trim()));
        if (!weightField.getText().trim().isEmpty())
            product.put("weight", Double.parseDouble(weightField.getText().trim()));
        if (!sizeField.getText().trim().isEmpty())
            product.put("size", sizeField.getText().trim());
        return product;
    }

    private void loadCategories() {
        SupabaseService.getCategoriesAsync().thenAccept(categories -> {
            Platform.runLater(() -> {
                categoryCombo.getItems().clear();
                for (int i = 0; i < categories.length(); i++) {
                    org.json.JSONObject cat = categories.getJSONObject(i);
                    categoryCombo.getItems().add(new CategoryItem(cat.getInt("id"), cat.getString("name")));
                }
            });
        });
    }

    private void loadSuppliers() {
        SupabaseServiceExtension.getSuppliersAsync().thenAccept(suppliers -> {
            Platform.runLater(() -> {
                supplierCombo.getItems().clear();
                for (int i = 0; i < suppliers.length(); i++) {
                    org.json.JSONObject sup = suppliers.getJSONObject(i);
                    supplierCombo.getItems().add(new SupplierItem(sup.getInt("id"), sup.getString("name")));
                }
            });
        });
    }
}
