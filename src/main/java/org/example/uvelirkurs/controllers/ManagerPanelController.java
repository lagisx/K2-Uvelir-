package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.example.uvelirkurs.BDandAPI.SupabaseServiceExtension;
import org.example.uvelirkurs.models.ProductData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;
public class ManagerPanelController {

    @FXML private TableView<ProductData> productsTable;
    @FXML private TableColumn<ProductData, Integer> productIdColumn;
    @FXML private TableColumn<ProductData, String> productNameColumn;
    @FXML private TableColumn<ProductData, String> materialColumn;
    @FXML private TableColumn<ProductData, Double> priceColumn;
    @FXML private TableColumn<ProductData, Integer> stockColumn;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        if (!hasManagerAccess()) {
            showAlert("Доступ запрещен", "У вас нет прав менеджера");
            goBack();
            return;
        }

        setupProductsTable();

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        loadProducts();
    }

    private boolean hasManagerAccess() {
        JSONObject user = SessionManager.getUser();
        if (user == null) return false;

        String role = user.optString("role");
        return "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    private void setupProductsTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        materialColumn.setCellValueFactory(new PropertyValueFactory<>("material"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewItem = new MenuItem("👁 Просмотр информации");
        MenuItem editItem = new MenuItem("✏ Редактировать");
        MenuItem addImagesItem = new MenuItem("🖼 Добавить изображения");
        MenuItem deleteItem = new MenuItem("🗑 Удалить");

        viewItem.setOnAction(e -> viewProductInfo());
        editItem.setOnAction(e -> editProduct());
        addImagesItem.setOnAction(e -> addProductImages());
        deleteItem.setOnAction(e -> deleteProduct());

        contextMenu.getItems().addAll(viewItem, editItem, addImagesItem, new SeparatorMenuItem(), deleteItem);
        productsTable.setContextMenu(contextMenu);
    }

    private void loadProducts() {
        showLoading(true);

        SupabaseService.getProductsAsync().thenAccept(products -> {
            Platform.runLater(() -> {
                productsTable.getItems().clear();
                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);
                    productsTable.getItems().add(new ProductData(
                            product.getInt("id"),
                            product.optString("name", ""),
                            product.optString("material", ""),
                            product.optDouble("price", 0),
                            product.optInt("stock_quantity", 0)
                    ));
                }
                showLoading(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showStatus("Ошибка загрузки товаров: " + ex.getMessage(), "error");
                showLoading(false);
            });
            return null;
        });
    }

    @FXML
    private void searchProducts() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            loadProducts();
            return;
        }

        productsTable.getItems().clear();
        SupabaseService.getProductsAsync().thenAccept(products -> {
            Platform.runLater(() -> {
                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);
                    String name = product.optString("name", "").toLowerCase();
                    String material = product.optString("material", "").toLowerCase();

                    if (name.contains(query) || material.contains(query)) {
                        productsTable.getItems().add(new ProductData(
                                product.getInt("id"),
                                product.optString("name", ""),
                                product.optString("material", ""),
                                product.optDouble("price", 0),
                                product.optInt("stock_quantity", 0)
                        ));
                    }
                }
            });
        });
    }

    @FXML
    private void addProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/add_product_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            AddProductDialogController controller = loader.getController();
            controller.setDialogPane(dialogPane);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Добавление товара");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                JSONObject productData = controller.getProductData();

                showLoading(true);
                SupabaseServiceExtension.addProduct(productData).thenAccept(productId -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (productId != null && productId > 0) {
                            showStatus("✅ Товар успешно добавлен (ID: " + productId + ")", "success");
                            loadProducts();

                            Alert imageAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            imageAlert.setTitle("Добавить изображения?");
                            imageAlert.setHeaderText("Товар успешно создан!");
                            imageAlert.setContentText("Хотите добавить изображения для нового товара?");

                            Optional<ButtonType> imageResult = imageAlert.showAndWait();
                            if (imageResult.isPresent() && imageResult.get() == ButtonType.OK) {
                                openAddImagesDialog(productId, productData.optString("name", "Товар #" + productId));
                            }
                        } else {
                            showStatus("❌ Ошибка при добавлении товара", "error");
                        }
                    });
                });
            }
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть диалог добавления товара: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editProduct() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите товар из таблицы");
            return;
        }

        JSONObject product = SupabaseService.getProductById(selected.getId());
        if (product == null) {
            showAlert("Ошибка", "Не удалось загрузить данные товара");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/org/example/uvelirkurs/edit_product_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            EditProductDialogController controller = loader.getController();
            controller.setDialogPane(dialogPane);
            controller.setProduct(product);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Редактирование товара");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                JSONObject updated = controller.getProductData();
                showLoading(true);
                SupabaseServiceExtension.updateProduct(
                        selected.getId(),
                        updated.optString("name", null),
                        updated.has("category_id") ? updated.getInt("category_id") : null,
                        updated.has("supplier_id") ? updated.getInt("supplier_id") : null,
                        updated.optString("description", null),
                        updated.optString("material", null),
                        updated.optString("purity", null),
                        updated.has("weight") ? updated.getDouble("weight") : null,
                        updated.optString("size", null),
                        updated.has("price") ? updated.getDouble("price") : null,
                        updated.has("cost_price") ? updated.getDouble("cost_price") : null,
                        updated.has("stock_quantity") ? updated.getInt("stock_quantity") : null,
                        updated.optString("collection", null)
                ).thenAccept(success -> Platform.runLater(() -> {
                    showLoading(false);
                    if (success) { showStatus("✅ Товар успешно обновлён", "success"); loadProducts(); }
                    else showStatus("❌ Ошибка обновления товара", "error");
                }));
            }
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть редактор: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addProductImages() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите товар из таблицы");
            return;
        }

        openAddImagesDialog(selected.getId(), selected.getName());
    }

    private void openAddImagesDialog(int productId, String productName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/add_images_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            AddImagesDialogController controller = loader.getController();
            controller.setDialogPane(dialogPane);
            controller.setProductInfo(productId, productName);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Добавление изображений");

            dialog.showAndWait();

            loadProducts();

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось открыть диалог добавления изображений: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteProduct() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите товар из таблицы");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление товара");
        confirm.setContentText(String.format(
                "Вы действительно хотите удалить товар:\n\n📦 %s\n\nЭто действие нельзя отменить!",
                selected.getName()
        ));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showLoading(true);
            SupabaseServiceExtension.deleteProduct(selected.getId()).thenAccept(success -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    if (success) {
                        showStatus("✅ Товар успешно удален", "success");
                        loadProducts();
                    } else {
                        showStatus("❌ Ошибка удаления товара", "error");
                    }
                });
            });
        }
    }

    private void viewProductInfo() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите товар из таблицы"); return; }

        JSONObject product = SupabaseService.getProductById(selected.getId());
        if (product == null) { showAlert("Ошибка", "Не удалось загрузить информацию о товаре"); return; }

        JSONArray images = SupabaseService.getProductImages(selected.getId());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Информация о товаре");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(0);
        content.setStyle("-fx-background-color: white;");

        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(12);
        header.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20 24;");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label icon = new Label("💎");
        icon.setStyle("-fx-font-size: 28px;");
        javafx.scene.layout.VBox hText = new javafx.scene.layout.VBox(4);
        Label hTitle = new Label(product.optString("name", ""));
        hTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label hSub = new Label("ID #" + product.optInt("id") + "  ·  " + product.optString("material", ""));
        hSub.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 13px;");
        hText.getChildren().addAll(hTitle, hSub);
        header.getChildren().addAll(icon, hText);

        javafx.scene.layout.HBox priceBadge = new javafx.scene.layout.HBox();
        priceBadge.setStyle("-fx-background-color: #4a5fd8; -fx-padding: 13 24;");
        priceBadge.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label priceLabel = new Label(String.format("%.2f ₽", product.optDouble("price", 0)));
        priceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 21px; -fx-font-weight: bold;");
        javafx.scene.layout.Region sp = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        Label costLabel = new Label(String.format("Себестоимость: %.2f ₽", product.optDouble("cost_price", 0)));
        costLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 13px;");
        priceBadge.getChildren().addAll(priceLabel, sp, costLabel);

        javafx.scene.layout.VBox fields = new javafx.scene.layout.VBox(0);
        fields.getChildren().addAll(
            infoRow("Проба",      product.optString("purity", "-"),               false),
            infoRow("Вес",        product.optDouble("weight", 0) + " г",           true),
            infoRow("Размер",     product.optString("size", "-"),                   false),
            infoRow("На складе",  product.optInt("stock_quantity", 0) + " шт.",     true),
            infoRow("Коллекция",  product.optString("collection", "-"),             false),
            infoRow("Описание",   product.optString("description", "-"),            true)
        );

        if (images.length() > 0) {
            javafx.scene.layout.VBox imgSection = new javafx.scene.layout.VBox(5);
            imgSection.setStyle("-fx-padding: 12 20; -fx-background-color: #f0f4ff;");
            Label imgTitle = new Label("🖼️  Изображения: " + images.length());
            imgTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5fd8; -fx-font-weight: bold;");
            imgSection.getChildren().add(imgTitle);
            for (int i = 0; i < images.length(); i++) {
                String url = images.getJSONObject(i).getString("image_url");
                int lastSlash = url.lastIndexOf('/');
                String shortUrl = lastSlash >= 0 ? "..." + url.substring(lastSlash) : url;
                Label urlLabel = new Label((i + 1) + ". " + shortUrl);
                urlLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
                imgSection.getChildren().add(urlLabel);
            }
            fields.getChildren().add(imgSection);
        }

        content.getChildren().addAll(header, priceBadge, fields);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(460);
        dialog.showAndWait();
    }

    private javafx.scene.layout.HBox infoRow(String label, String value, boolean alt) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox();
        row.setStyle("-fx-padding: 11 20; -fx-background-color: " + (alt ? "#fafafa" : "white") + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-min-width: 110;");
        Label val = new Label(value == null || value.isEmpty() ? "—" : value);
        val.setStyle("-fx-text-fill: #222; -fx-font-size: 13px; -fx-font-weight: bold;");
        val.setWrapText(true);
        javafx.scene.layout.Region sp = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().addAll(lbl, sp, val);
        return row;
    }

    @FXML
    private void logout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Выход из аккаунта");
        confirmAlert.setHeaderText("Вы уверены?");
        confirmAlert.setContentText("Вы действительно хотите выйти из аккаунта?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.logout();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/login.fxml"));
                Stage stage = (Stage) productsTable.getScene().getWindow();
                Scene scene = new Scene(loader.load());
                stage.setMaximized(true);
                stage.setScene(scene);
                stage.setTitle("Uvelir Shop - Вход");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goBack() {
        try {
            if (productsTable == null || productsTable.getScene() == null) return;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
            Stage stage = (Stage) productsTable.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());

            MainController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getUser());

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    private void showStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setVisible(true);

            if ("success".equals(type)) {
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else if ("error".equals(type)) {
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-font-weight: bold;");
            }

            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                    Platform.runLater(() -> statusLabel.setVisible(false));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}