package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
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
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        loadUsers();
        loadProducts();
    }

    private boolean hasAdminAccess() {
        JSONObject user = SessionManager.getUser();
        return user != null && "ADMIN".equals(user.optString("role"));
    }

    private void setupUsersTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        fullnameColumn.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        usersTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(UserData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isActive()) {
                    setStyle("-fx-background-color: #fee2e2;");
                } else {
                    setStyle("");
                }
            }
        });

        ContextMenu userContextMenu = new ContextMenu();
        MenuItem viewUserItem = new MenuItem("👁 Просмотр");
        MenuItem editUserItem = new MenuItem("✏ Редактировать");
        MenuItem blockUserItem = new MenuItem("🔒 Заблокировать / Разблокировать");
        MenuItem deleteUserItem = new MenuItem("🗑 Удалить");

        viewUserItem.setOnAction(e -> viewUserInfo());
        editUserItem.setOnAction(e -> editUser());
        blockUserItem.setOnAction(e -> toggleBlockUser());
        deleteUserItem.setOnAction(e -> deleteUser());

        userContextMenu.getItems().addAll(viewUserItem, editUserItem, new SeparatorMenuItem(), blockUserItem, new SeparatorMenuItem(), deleteUserItem);
        usersTable.setContextMenu(userContextMenu);
    }

    private void setupProductsTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        materialColumn.setCellValueFactory(new PropertyValueFactory<>("material"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        ContextMenu productContextMenu = new ContextMenu();
        MenuItem viewProductItem = new MenuItem("👁 Просмотр информации");
        MenuItem deleteProductItem = new MenuItem("🗑 Удалить");

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
                        usersTable.getItems().add(new UserData(
                                user.optInt("id", -1),
                                user.optString("username", ""),
                                user.optString("email", ""),
                                user.optString("fullname", ""),
                                user.optString("phone", ""),
                                user.optString("role", "CLIENT"),
                                user.optBoolean("is_active", true)
                        ));
                    }
                }
                showLoading(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> { showStatus("Ошибка загрузки пользователей", "error"); showLoading(false); });
            return null;
        });
    }

    private void loadProducts() {
        showLoading(true);
        SupabaseService.getProductsAsync().thenAccept(products -> {
            Platform.runLater(() -> {
                productsTable.getItems().clear();
                if (products != null && products.length() > 0) {
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject product = products.getJSONObject(i);
                        productsTable.getItems().add(new ProductData(
                                product.optInt("id", -1),
                                product.optString("name", ""),
                                product.optString("material", ""),
                                product.optDouble("price", 0),
                                product.optInt("stock_quantity", 0)
                        ));
                    }
                }
                showLoading(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> { showStatus("Ошибка загрузки товаров", "error"); showLoading(false); });
            return null;
        });
    }

    @FXML
    private void searchUsers() {
        String query = searchUserField.getText().toLowerCase().trim();
        if (query.isEmpty()) { loadUsers(); return; }
        showLoading(true);
        usersTable.getItems().clear();
        SupabaseServiceExtension.getAllUsersAsync().thenAccept(users -> {
            Platform.runLater(() -> {
                if (users != null) {
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        if (user.optString("username","").toLowerCase().contains(query) ||
                            user.optString("email","").toLowerCase().contains(query) ||
                            user.optString("fullname","").toLowerCase().contains(query)) {
                            usersTable.getItems().add(new UserData(
                                    user.optInt("id", -1),
                                    user.optString("username", ""),
                                    user.optString("email", ""),
                                    user.optString("fullname", ""),
                                    user.optString("phone", ""),
                                    user.optString("role", "CLIENT"),
                                    user.optBoolean("is_active", true)
                            ));
                        }
                    }
                }
                showLoading(false);
            });
        }).exceptionally(ex -> { Platform.runLater(() -> showLoading(false)); return null; });
    }

    @FXML
    private void searchProducts() {
        String query = searchProductField.getText().toLowerCase().trim();
        if (query.isEmpty()) { loadProducts(); return; }
        showLoading(true);
        productsTable.getItems().clear();
        SupabaseService.getProductsAsync().thenAccept(products -> {
            Platform.runLater(() -> {
                if (products != null) {
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject product = products.getJSONObject(i);
                        if (product.optString("name","").toLowerCase().contains(query) ||
                            product.optString("material","").toLowerCase().contains(query)) {
                            productsTable.getItems().add(new ProductData(
                                    product.optInt("id", -1),
                                    product.optString("name", ""),
                                    product.optString("material", ""),
                                    product.optDouble("price", 0),
                                    product.optInt("stock_quantity", 0)
                            ));
                        }
                    }
                }
                showLoading(false);
            });
        }).exceptionally(ex -> { Platform.runLater(() -> showLoading(false)); return null; });
    }

    private void viewUserInfo() {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите пользователя"); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Пользователь #" + selected.getId());

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: white;");

        HBox header = new HBox(12);
        header.setStyle("-fx-background-color: #4a5fd8; -fx-padding: 20 24;");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label avatarLbl = new Label(selected.getUsername().isEmpty() ? "?" : selected.getUsername().substring(0,1).toUpperCase());
        avatarLbl.setStyle("-fx-background-color: white; -fx-text-fill: #4a5fd8; -fx-font-size: 20px; -fx-font-weight: bold; -fx-min-width: 44; -fx-min-height: 44; -fx-max-width: 44; -fx-max-height: 44; -fx-background-radius: 22; -fx-alignment: CENTER;");
        VBox headerText = new VBox(3);
        Label nameLbl = new Label(selected.getFullname().isEmpty() ? selected.getUsername() : selected.getFullname());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label roleLbl = new Label(roleToRussian(selected.getRole()) + (selected.isActive() ? "" : " · Заблокирован"));
        roleLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;");
        headerText.getChildren().addAll(nameLbl, roleLbl);
        header.getChildren().addAll(avatarLbl, headerText);

        VBox fields = new VBox(0);
        fields.setStyle("-fx-padding: 0;");
        fields.getChildren().addAll(
            infoRow("ID", String.valueOf(selected.getId()), false),
            infoRow("Логин", selected.getUsername(), true),
            infoRow("Email", selected.getEmail(), false),
            infoRow("Полное имя", selected.getFullname(), true),
            infoRow("Телефон", selected.getPhone(), false),
            infoRow("Роль", roleToRussian(selected.getRole()), true),
            infoRow("Статус", selected.isActive() ? "✅ Активен" : "🔒 Заблокирован", false)
        );

        content.getChildren().addAll(header, fields);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(380);
        dialog.showAndWait();
    }

    private HBox infoRow(String label, String value, boolean alt) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 12 20; -fx-background-color: " + (alt ? "#fafafa" : "white") + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-min-width: 110;");
        Label val = new Label(value.isEmpty() ? "—" : value);
        val.setStyle("-fx-text-fill: #222; -fx-font-size: 13px; -fx-font-weight: bold;");
        val.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private void editUser() {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите пользователя"); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование пользователя");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: white; -fx-min-width: 400;");

        HBox header = new HBox();
        header.setStyle("-fx-background-color: #f8f9ff; -fx-padding: 18 20; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        Label title = new Label("Пользователь #" + selected.getId());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        header.getChildren().add(title);

        VBox form = new VBox(16);
        form.setStyle("-fx-padding: 20;");

        TextField usernameField = styledField(selected.getUsername());
        TextField fullnameField = styledField(selected.getFullname());
        TextField emailField = styledField(selected.getEmail());
        TextField phoneField = styledField(selected.getPhone());

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("CLIENT", "MANAGER", "ADMIN");
        roleCombo.setValue(selected.getRole());
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle("-fx-font-size: 14px;");
        roleCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? "" : roleToRussian(item));
            }
        });
        roleCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? "" : roleToRussian(item));
            }
        });

        form.getChildren().addAll(
            formRow("Имя пользователя", usernameField),
            formRow("Полное имя", fullnameField),
            formRow("Email", emailField),
            formRow("Телефон", phoneField),
            formRow("Роль", roleCombo)
        );

        content.getChildren().addAll(header, form);
        dialog.getDialogPane().setContent(content);

        Platform.runLater(() -> {
            Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            if (saveBtn != null) saveBtn.setStyle("-fx-background-color: #4a5fd8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        });

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
                ).thenAccept(success -> Platform.runLater(() -> {
                    showLoading(false);
                    if (success) { showStatus("Пользователь обновлён", "success"); loadUsers(); }
                    else showStatus("Ошибка обновления пользователя", "error");
                }));
            }
        });
    }

    private TextField styledField(String value) {
        TextField tf = new TextField(value);
        tf.setStyle("-fx-font-size: 14px; -fx-padding: 9 12; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");
        return tf;
    }

    private VBox formRow(String label, javafx.scene.Node field) {
        VBox box = new VBox(6);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void toggleBlockUser() {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите пользователя"); return; }

        JSONObject currentUser = SessionManager.getUser();
        if (currentUser != null && currentUser.optInt("id") == selected.getId()) {
            showAlert("Ошибка", "Вы не можете заблокировать себя!");
            return;
        }

        boolean willBlock = selected.isActive();
        String action = willBlock ? "заблокировать" : "разблокировать";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText(willBlock ? "Блокировка пользователя" : "Разблокировка пользователя");
        confirm.setContentText("Вы действительно хотите " + action + " пользователя " + selected.getUsername() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showLoading(true);
                SupabaseServiceExtension.setUserBlocked(selected.getId(), willBlock)
                        .thenAccept(success -> Platform.runLater(() -> {
                            showLoading(false);
                            if (success) {
                                showStatus("Пользователь " + (willBlock ? "заблокирован" : "разблокирован"), "success");
                                loadUsers();
                            } else {
                                showStatus("Ошибка изменения статуса пользователя", "error");
                            }
                        }));
            }
        });
    }

    private void deleteUser() {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите пользователя"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удаление пользователя");
        confirm.setContentText("Удалить пользователя " + selected.getUsername() + "? Действие нельзя отменить!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showLoading(true);
                SupabaseServiceExtension.deleteUser(selected.getId()).thenAccept(success -> Platform.runLater(() -> {
                    showLoading(false);
                    if (success) { showStatus("Пользователь удалён", "success"); loadUsers(); }
                    else showStatus("Ошибка удаления пользователя", "error");
                }));
            }
        });
    }

    private void viewProductInfo() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите товар"); return; }

        JSONObject product = SupabaseService.getProductById(selected.getId());
        if (product == null) { showAlert("Ошибка", "Не удалось загрузить товар"); return; }

        JSONArray images = SupabaseService.getProductImages(selected.getId());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Информация о товаре");

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: white;");

        HBox header = new HBox(12);
        header.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 20 24;");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label icon = new Label("💎");
        icon.setStyle("-fx-font-size: 28px;");
        VBox hText = new VBox(3);
        Label hTitle = new Label(product.optString("name", ""));
        hTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label hSub = new Label("ID #" + product.optInt("id") + " · " + product.optString("material", ""));
        hSub.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px;");
        hText.getChildren().addAll(hTitle, hSub);
        header.getChildren().addAll(icon, hText);

        HBox priceBadge = new HBox();
        priceBadge.setStyle("-fx-background-color: #4a5fd8; -fx-padding: 14 24;");
        Label priceLabel = new Label(String.format("%.2f ₽", product.optDouble("price", 0)));
        priceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label costLabel = new Label(String.format("  Себестоимость: %.2f ₽", product.optDouble("cost_price", 0)));
        costLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px;");
        costLabel.setAlignment(javafx.geometry.Pos.CENTER);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        priceBadge.getChildren().addAll(priceLabel, spacer, costLabel);
        priceBadge.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox fields = new VBox(0);
        fields.getChildren().addAll(
            infoRow("Проба", product.optString("purity", "-"), false),
            infoRow("Вес", product.optDouble("weight", 0) + " г", true),
            infoRow("Размер", product.optString("size", "-"), false),
            infoRow("На складе", product.optInt("stock_quantity", 0) + " шт.", true),
            infoRow("Коллекция", product.optString("collection", "-"), false),
            infoRow("Описание", product.optString("description", "-"), true)
        );

        if (images.length() > 0) {
            VBox imgSection = new VBox(8);
            imgSection.setStyle("-fx-padding: 12 20; -fx-background-color: #f8f9ff;");
            Label imgTitle = new Label("Изображений: " + images.length());
            imgTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-font-weight: bold;");
            imgSection.getChildren().add(imgTitle);
            for (int i = 0; i < images.length(); i++) {
                String url = images.getJSONObject(i).getString("image_url");
                Label urlLabel = new Label((i+1) + ". " + shortenUrl(url));
                urlLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5fd8;");
                urlLabel.setWrapText(true);
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

    private String shortenUrl(String url) {
        if (url == null) return "";
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? "..." + url.substring(lastSlash) : url;
    }

    private void deleteProduct() {
        ProductData selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Ошибка", "Выберите товар"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удаление товара");
        confirm.setContentText("Удалить товар " + selected.getName() + "? Действие нельзя отменить!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showLoading(true);
                SupabaseServiceExtension.deleteProduct(selected.getId()).thenAccept(success -> Platform.runLater(() -> {
                    showLoading(false);
                    if (success) { showStatus("Товар удалён", "success"); loadProducts(); }
                    else showStatus("Ошибка удаления товара", "error");
                }));
            }
        });
    }

    private String roleToRussian(String role) {
        return switch (role) {
            case "ADMIN" -> "Администратор";
            case "MANAGER" -> "Менеджер";
            default -> "Клиент";
        };
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) loadingIndicator.setVisible(show);
    }

    private void showStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + ("error".equals(type) ? "#dc2626" : "success".equals(type) ? "#16a34a" : "#2563eb") + ";");
            statusLabel.setVisible(true);
            new Thread(() -> {
                try { Thread.sleep(4000); Platform.runLater(() -> statusLabel.setVisible(false)); }
                catch (InterruptedException ignored) {}
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

    @FXML
    private void logout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Выход");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Выйти из аккаунта?");
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
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void goBack() {
        try {
            if (usersTable == null || usersTable.getScene() == null) return;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/mainmenu.fxml"));
            Stage stage = (Stage) usersTable.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            MainController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getUser());
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
