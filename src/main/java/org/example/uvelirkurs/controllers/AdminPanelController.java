package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseServiceExtension;
import org.example.uvelirkurs.models.ProductData;
import org.example.uvelirkurs.models.UserData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;

public class AdminPanelController {

    @FXML private TableView<UserData> usersTable;
    @FXML private TableColumn<UserData, Integer> userIdColumn;
    @FXML private TableColumn<UserData, String> usernameColumn;
    @FXML private TableColumn<UserData, String> emailColumn;
    @FXML private TableColumn<UserData, String> fullnameColumn;
    @FXML private TableColumn<UserData, String> phoneColumn;
    @FXML private TableColumn<UserData, String> roleColumn;

    @FXML private TableView<ProductData> productsTable;
    @FXML private TableColumn<ProductData, Integer> productIdColumn;
    @FXML private TableColumn<ProductData, String> productNameColumn;
    @FXML private TableColumn<ProductData, String> materialColumn;
    @FXML private TableColumn<ProductData, Double> priceColumn;
    @FXML private TableColumn<ProductData, Integer> stockColumn;

    @FXML private TextField searchUserField;
    @FXML private TextField searchProductField;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {

        if (!hasAdminAccess()) {
            showAlert("Доступ запрещен", "У вас нет прав администратора");
            goBack();
            return;
        }

        setupUsersTable();
        setupProductsTable();

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        loadUsers();
        loadProducts();
    }

    private boolean hasAdminAccess() {
        JSONObject user = SessionManager.getUser();
        if (user == null) {
            return false;
        }

        String role = user.optString("role");

        if (!"ADMIN".equals(role)) {
            return false;
        }

        return true;
    }

    private void setupUsersTable() {

        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        fullnameColumn.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        ContextMenu userContextMenu = new ContextMenu();
        MenuItem viewUserItem = new MenuItem("Просмотр информации");
        MenuItem editUserItem = new MenuItem("Редактировать");
        MenuItem deleteUserItem = new MenuItem("Удалить");

        viewUserItem.setOnAction(e -> viewUserInfo());
        editUserItem.setOnAction(e -> editUser());
        deleteUserItem.setOnAction(e -> deleteUser());

        userContextMenu.getItems().addAll(viewUserItem, editUserItem, deleteUserItem);
        usersTable.setContextMenu(userContextMenu);
    }

    private void setupProductsTable() {

        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        materialColumn.setCellValueFactory(new PropertyValueFactory<>("material"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        ContextMenu productContextMenu = new ContextMenu();
        MenuItem viewProductItem = new MenuItem("Просмотр информации");
        MenuItem deleteProductItem = new MenuItem("Удалить");

        viewProductItem.setOnAction(e -> viewProductInfo());
        deleteProductItem.setOnAction(e -> deleteProduct());

        productContextMenu.getItems().addAll(viewProductItem, deleteProductItem);
        productsTable.setContextMenu(productContextMenu);
    }

    private void loadUsers() {
        showLoading(true);

        SupabaseServiceExtension.getAllUsersAsync().thenAccept(users -> {

            Platform.runLater(() -> {
                usersTable.getItems().clear();
                if (users != null && users.length() > 0) {
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        UserData userData = new UserData(
                                user.optInt("id", -1),
                                user.optString("username", ""),
                                user.optString("email", ""),
                                user.optString("fullname", ""),
                                user.optString("phone", ""),
                                user.optString("role", "CLIENT")
                        );
                        usersTable.getItems().add(userData);
                    }
                    showStatus("Пользователи загружены успешно (" + usersTable.getItems().size() + ")", "success");
                } else {
                    showStatus("Нет пользователей для отображения", "info");
                }
                showLoading(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showStatus("Ошибка загрузки пользователей: " + ex.getMessage(), "error");
                showLoading(false);
            });
            ex.printStackTrace();
            return null;
        });
    }

    private void loadProducts() {
        showLoading(true);

        org.example.uvelirkurs.BDandAPI.SupabaseService.getProductsAsync().thenAccept(products -> {

            Platform.runLater(() -> {
                productsTable.getItems().clear();
                if (products != null && products.length() > 0) {
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject product = products.getJSONObject(i);
                        ProductData productData = new ProductData(
                                product.optInt("id", -1),
                                product.optString("name", ""),
                                product.optString("material", ""),
                                product.optDouble("price", 0),
                                product.optInt("stock_quantity", 0)
                        );
                        productsTable.getItems().add(productData);

                    }
                    showStatus("Товары загружены успешно (" + productsTable.getItems().size() + ")", "success");
                } else {
                    showStatus("Нет товаров для отображения", "info");
                }
                showLoading(false);

            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showStatus("Ошибка загрузки товаров: " + ex.getMessage(), "error");
                showLoading(false);
            });
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    private void searchUsers() {
        String query = searchUserField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            loadUsers();
            return;
        }

        showLoading(true);
        usersTable.getItems().clear();
        SupabaseServiceExtension.getAllUsersAsync().thenAccept(users -> {
            Platform.runLater(() -> {
                if (users != null) {
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        String username = user.optString("username", "").toLowerCase();
                        String email = user.optString("email", "").toLowerCase();
                        String fullname = user.optString("fullname", "").toLowerCase();

                        if (username.contains(query) || email.contains(query) || fullname.contains(query)) {
                            usersTable.getItems().add(new UserData(
                                    user.optInt("id", -1),
                                    user.optString("username", ""),
                                    user.optString("email", ""),
                                    user.optString("fullname", ""),
                                    user.optString("phone", ""),
                                    user.optString("role", "CLIENT")
                            ));
                        }
                    }
                    if (usersTable.getItems().isEmpty()) {
                        showStatus("Нет результатов поиска для пользователей", "info");
                    }
                }
                showLoading(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showStatus("Ошибка поиска пользователей: " + ex.getMessage(), "error");
                showLoading(false);
            });
            return null;
        });
    }

    @FXML
    private void searchProducts() {
        String query = searchProductField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            loadProducts();
            return;
        }

        showLoading(true);
        productsTable.getItems().clear();
        org.example.uvelirkurs.BDandAPI.SupabaseService.getProductsAsync().thenAccept(products -> {
            Platform.runLater(() -> {
                if (products != null) {
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject product = products.getJSONObject(i);
                        String name = product.optString("name", "").toLowerCase();
                        String material = product.optString("material", "").toLowerCase();

                        if (name.contains(query) || material.contains(query)) {
                            productsTable.getItems().add(new ProductData(
                                    product.optInt("id", -1),
                                    product.optString("name", ""),
                                    product.optString("material", ""),
                                    product.optDouble("price", 0),
                                    product.optInt("stock_quantity", 0)
                            ));
                        }
                    }
                    if (productsTable.getItems().isEmpty()) {
                        showStatus("Нет результатов поиска для товаров", "info");
                    }
                }
                showLoading(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                showStatus("Ошибка поиска товаров: " + ex.getMessage(), "error");
                showLoading(false);
            });
            return null;
        });
    }

    private void viewUserInfo() {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите пользователя");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о пользователе");
        alert.setHeaderText("Пользователь #" + selected.getId());

        String info = String.format(
                "ID: %d\nИмя пользователя: %s\nEmail: %s\nПолное имя: %s\nТелефон: %s\nРоль: %s",
                selected.getId(),
                selected.getUsername(),
                selected.getEmail(),
                selected.getFullname(),
                selected.getPhone(),
                selected.getRole()
        );

        alert.setContentText(info);
        alert.showAndWait();
    }

    private void editUser() {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите пользователя");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование пользователя");
        dialog.setHeaderText("Пользователь #" + selected.getId());

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);

        TextField usernameField = new TextField(selected.getUsername());
        TextField fullnameField = new TextField(selected.getFullname());
        TextField emailField = new TextField(selected.getEmail());
        TextField phoneField = new TextField(selected.getPhone());
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("CLIENT", "MANAGER", "ADMIN");
        roleCombo.setValue(selected.getRole());

        content.getChildren().addAll(
                new Label("Имя пользователя:"), usernameField,
                new Label("Полное имя:"), fullnameField,
                new Label("Email:"), emailField,
                new Label("Телефон:"), phoneField,
                new Label("Роль:"), roleCombo
        );

        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                showLoading(true);
                SupabaseServiceExtension.updateUserByAdmin(
                        selected.getId(),
                        usernameField.getText().trim(),
                        fullnameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        roleCombo.getValue()
                ).thenAccept(success -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (success) {
                            showStatus("Пользователь успешно обновлен", "success");
                            loadUsers();
                        } else {
                            showStatus("Ошибка обновления пользователя", "error");
                        }
                    });
                });
            }
        });
    }

    private void deleteUser() {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите пользователя");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удаление пользователя");
        confirm.setContentText("Вы действительно хотите удалить пользователя " + selected.getUsername() + "?\nЭто действие нельзя отменить!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showLoading(true);
                SupabaseServiceExtension.deleteUser(selected.getId()).thenAccept(success -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (success) {
                            showStatus("Пользователь успешно удален", "success");
                            loadUsers();
                        } else {
                            showStatus("Ошибка удаления пользователя", "error");
                        }
                    });
                });
            }
        });
    }

    private void viewProductInfo() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите товар");
            return;
        }

        JSONObject product = org.example.uvelirkurs.BDandAPI.SupabaseService.getProductById(selected.getId());
        if (product == null) {
            showAlert("Ошибка", "Не удалось загрузить информацию о товаре");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о товаре");
        alert.setHeaderText("Товар #" + selected.getId());

        String info = String.format(
                "ID: %d\nНазвание: %s\nМатериал: %s\nЦена: %.2f ₽\nСебестоимость: %.2f ₽\n" +
                        "Проба: %s\nВес: %.2f г\nРазмер: %s\nКоличество: %d\nКоллекция: %s\nОписание: %s",
                product.optInt("id", -1),
                product.optString("name", ""),
                product.optString("material", ""),
                product.optDouble("price", 0),
                product.optDouble("cost_price", 0),
                product.optString("purity", ""),
                product.optDouble("weight", 0),
                product.optString("size", ""),
                product.optInt("stock_quantity", 0),
                product.optString("collection", ""),
                product.optString("description", "")
        );

        alert.setContentText(info);
        alert.showAndWait();
    }

    private void deleteProduct() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите товар");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удаление товара");
        confirm.setContentText("Вы действительно хотите удалить товар " + selected.getName() + "?\nЭто действие нельзя отменить!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showLoading(true);
                SupabaseServiceExtension.deleteProduct(selected.getId()).thenAccept(success -> {
                    Platform.runLater(() -> {
                        showLoading(false);
                        if (success) {
                            showStatus("Товар успешно удален", "success");
                            loadProducts();
                        } else {
                            showStatus("Ошибка удаления товара", "error");
                        }
                    });
                });
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }

    private void showStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            if ("error".equals(type)) {
                statusLabel.setStyle("-fx-text-fill: red;");
            } else if ("success".equals(type)) {
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setStyle("-fx-text-fill: blue;");
            }
            statusLabel.setVisible(true);

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
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

    // ДОБАВЬТЕ ЭТОТ МЕТОД В AdminPanelController.java

    /**
     * Выход из системы
     */
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
                Stage stage = (Stage) usersTable.getScene().getWindow();
                Scene scene = new Scene(loader.load());
                stage.setMaximized(true);
                stage.setScene(scene);
                stage.setTitle("Uvelir Shop - Вход");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Ошибка", "Не удалось перейти к экрану входа");
            }
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
            Stage stage = (Stage) usersTable.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());

            MainController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getUser());

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться в главное меню");
        }
    }

}