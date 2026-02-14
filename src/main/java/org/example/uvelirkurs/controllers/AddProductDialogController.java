package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.example.uvelirkurs.BDandAPI.SupabaseServiceExtension;
import org.example.uvelirkurs.models.CategoryItem;
import org.example.uvelirkurs.models.SupplierItem;
import org.json.JSONObject;

/**
 * Контроллер диалога добавления товара
 */
public class AddProductDialogController {

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

    @FXML
    public void initialize() {
        loadCategories();
        loadSuppliers();
        setupValidation();
    }

    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
    }

    private void setupValidation() {
        if (dialogPane != null) {
            Button createButton = (Button) dialogPane.lookupButton(
                    dialogPane.getButtonTypes().stream()
                            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            .findFirst()
                            .orElse(null)
            );

            if (createButton != null) {
                createButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!validateInput()) {
                        event.consume();
                    }
                });
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("• Название товара обязательно\n");
        }

        if (categoryCombo.getValue() == null) {
            errors.append("• Выберите категорию\n");
        }

        if (supplierCombo.getValue() == null) {
            errors.append("• Выберите поставщика\n");
        }

        if (materialField.getText().trim().isEmpty()) {
            errors.append("• Материал обязателен\n");
        }

        if (priceField.getText().trim().isEmpty()) {
            errors.append("• Цена продажи обязательна\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    errors.append("• Цена должна быть больше 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Цена должна быть числом\n");
            }
        }

        if (stockField.getText().trim().isEmpty()) {
            errors.append("• Количество на складе обязательно\n");
        } else {
            try {
                int stock = Integer.parseInt(stockField.getText().trim());
                if (stock < 0) {
                    errors.append("• Количество не может быть отрицательным\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Количество должно быть целым числом\n");
            }
        }

        if (!weightField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(weightField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("• Вес должен быть числом\n");
            }
        }

        if (!costPriceField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(costPriceField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("• Себестоимость должна быть числом\n");
            }
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка валидации");
            alert.setHeaderText("Пожалуйста, исправьте следующие ошибки:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    public JSONObject getProductData() {
        JSONObject product = new JSONObject();

        product.put("name", nameField.getText().trim());
        product.put("category_id", categoryCombo.getValue().getId());
        product.put("supplier_id", supplierCombo.getValue().getId());
        product.put("description", descriptionArea.getText().trim());
        product.put("material", materialField.getText().trim());
        product.put("purity", purityField.getText().trim());
        product.put("price", Double.parseDouble(priceField.getText().trim()));
        product.put("stock_quantity", Integer.parseInt(stockField.getText().trim()));
        product.put("collection", collectionField.getText().trim());

        if (!weightField.getText().trim().isEmpty()) {
            product.put("weight", Double.parseDouble(weightField.getText().trim()));
        }

        if (!sizeField.getText().trim().isEmpty()) {
            product.put("size", sizeField.getText().trim());
        }

        if (!costPriceField.getText().trim().isEmpty()) {
            product.put("cost_price", Double.parseDouble(costPriceField.getText().trim()));
        }

        return product;
    }

    private void loadCategories() {
        SupabaseService.getCategoriesAsync().thenAccept(categories -> {
            Platform.runLater(() -> {
                categoryCombo.getItems().clear();
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject cat = categories.getJSONObject(i);
                    categoryCombo.getItems().add(new CategoryItem(
                            cat.getInt("id"),
                            cat.getString("name")
                    ));
                }
                if (!categoryCombo.getItems().isEmpty()) {
                    categoryCombo.getSelectionModel().selectFirst();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setContentText("Не удалось загрузить категории: " + ex.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }

    private void loadSuppliers() {
        SupabaseServiceExtension.getSuppliersAsync().thenAccept(suppliers -> {
            Platform.runLater(() -> {
                supplierCombo.getItems().clear();
                for (int i = 0; i < suppliers.length(); i++) {
                    JSONObject sup = suppliers.getJSONObject(i);
                    supplierCombo.getItems().add(new SupplierItem(
                            sup.getInt("id"),
                            sup.getString("name")
                    ));
                }
                if (!supplierCombo.getItems().isEmpty()) {
                    supplierCombo.getSelectionModel().selectFirst();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setContentText("Не удалось загрузить поставщиков: " + ex.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }
}