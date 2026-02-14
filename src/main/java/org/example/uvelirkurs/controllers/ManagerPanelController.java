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


        showAlert("В разработке :(", "Пока что нет такой функции");
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
        if (selected == null) {
            showAlert("Ошибка", "Выберите товар из таблицы");
            return;
        }

        JSONObject product = SupabaseService.getProductById(selected.getId());
        if (product == null) {
            showAlert("Ошибка", "Не удалось загрузить информацию о товаре");
            return;
        }

        JSONArray images = SupabaseService.getProductImages(selected.getId());
        StringBuilder imageUrls = new StringBuilder();
        if (images.length() > 0) {
            for (int i = 0; i < images.length(); i++) {
                imageUrls.append("\n  ").append(i + 1).append(". ")
                        .append(images.getJSONObject(i).getString("image_url"));
            }
        } else {
            imageUrls.append("\n  Нет изображений");
        }

        String info = String.format(
                        "Название: %s\n" +
                        "Материал: %s\n" +
                        "Проба: %s\n" +
                        "⚖Вес: %.2f г\n" +
                        "Размер: %s\n" +
                        "Цена: %.2f ₽\n" +
                        "Себестоимость: %.2f ₽\n" +
                        "На складе: %d шт.\n" +
                        "Коллекция: %s\n" +
                        "Описание:\n%s\n" +
                        "Изображения:%s",
                product.optString("name", "-"),
                product.optString("material", "-"),
                product.optString("purity", "-"),
                product.optDouble("weight", 0),
                product.optString("size", "-"),
                product.optDouble("price", 0),
                product.optDouble("cost_price", 0),
                product.optInt("stock_quantity", 0),
                product.optString("collection", "-"),
                product.optString("description", "Нет описания"),
                imageUrls.toString()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о товаре");
        alert.setHeaderText("📦 Товар #" + selected.getId());
        alert.setContentText(info);
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
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