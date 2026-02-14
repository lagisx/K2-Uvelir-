package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.uvelirkurs.BDandAPI.CartManager;
import org.example.uvelirkurs.BDandAPI.SessionManager;
import org.example.uvelirkurs.BDandAPI.SupabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainController {

    @FXML private FlowPane productsPane;
    @FXML private VBox categoriesBox;
    @FXML private Button profileButton;
    @FXML private Button cartButton;
    @FXML private Label cartBadge;
    @FXML private Button ordersButton;
    @FXML private Label productCountLabel;
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator loadingIndicator;

    private CartManager cartManager;

    @FXML
    public void initialize() {
        cartManager = CartManager.getInstance();
        
        JSONObject user = SessionManager.getUser();
        if (user != null) {
            cartManager.loadCartFromDB(user.getInt("id"));
        }

        loadProductsFromSupabase();
        loadCategories();

        profileButton.setOnAction(e -> openProfile());
        cartButton.setOnAction(e -> openCart());
        ordersButton.setOnAction(e -> openOrderHistory());

        cartManager.getCartItems().addListener((javafx.collections.ListChangeListener.Change<? extends org.example.uvelirkurs.models.CartItem> c) -> {
            updateCartBadge();
        });

        updateCartBadge();
        
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
        }
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
        }
    }

    private void updateCartBadge() {
        int count = cartManager.getTotalItemsCount();
        if (count > 0) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(true);
        } else {
            cartBadge.setVisible(false);
        }
    }

    public void setCurrentUser(JSONObject user) {
        SessionManager.setUser(user);
        if (user != null) {
            cartManager.loadCartFromDB(user.getInt("id"));
        }
    }

    private void loadProductsFromSupabase() {
        showLoading(true);
        SupabaseService.getProductsAsync().thenAccept(products -> {
            Platform.runLater(() -> {
                renderProducts(products);
                showLoading(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showLoading(false));
            ex.printStackTrace();
            return null;
        });
    }

    private void loadCategories() {
        SupabaseService.getCategoriesAsync().thenAccept(categories -> {
            Platform.runLater(() -> {
                categoriesBox.getChildren().clear();

                Button allBtn = createCategoryButton("Все товары", true);
                allBtn.setOnAction(e -> {
                    loadProductsFromSupabase();
                    highlightSelectedCategory(allBtn);
                });
                categoriesBox.getChildren().add(allBtn);

                for (int i = 0; i < categories.length(); i++) {
                    JSONObject cat = categories.getJSONObject(i);
                    String categoryName = cat.getString("name");
                    int categoryId = cat.getInt("id");

                    Button btn = createCategoryButton(categoryName, false);
                    btn.setOnAction(e -> {
                        loadProductsByCategory(categoryId);
                        highlightSelectedCategory(btn);
                    });

                    categoriesBox.getChildren().add(btn);
                }
            });
        });
    }

    private Button createCategoryButton(String text, boolean isSelected) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        
        if (isSelected) {
            btn.getStyleClass().add("category-button-selected");
        } else {
            btn.getStyleClass().add("category-button");
        }
        
        return btn;
    }

    private void highlightSelectedCategory(Button selectedBtn) {
        for (javafx.scene.Node node : categoriesBox.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.getStyleClass().removeAll("category-button", "category-button-selected");
                
                if (btn == selectedBtn) {
                    btn.getStyleClass().add("category-button-selected");
                } else {
                    btn.getStyleClass().add("category-button");
                }
            }
        }
    }

    private void loadProductsByCategory(int categoryId) {
        showLoading(true);
        new Thread(() -> {
            JSONArray products = SupabaseService.getProductsByCategory(categoryId);
            Platform.runLater(() -> {
                renderProducts(products);
                showLoading(false);
            });
        }).start();
    }

    private void renderProducts(JSONArray products) {
        productsPane.getChildren().clear();

        if (productCountLabel != null) {
            int count = products.length();
            productCountLabel.setText(count + " " + getProductWord(count));
        }

        for (int i = 0; i < products.length(); i++) {
            addProductCard(products.getJSONObject(i));
        }
    }

    private String getProductWord(int count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "товар";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return "товара";
        } else {
            return "товаров";
        }
    }

    public void openProfile() {
        JSONObject user = SessionManager.getUser();
        if (user == null) {
            System.out.println("Ошибка: пользователь не авторизован!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/profile.fxml"));
            
            Stage stage = (Stage) profileButton.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            
            ProfileController controller = loader.getController();
            controller.setCurrentUser(user);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/cart.fxml"));
            
            Stage stage = (Stage) cartButton.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            
            CartController controller = loader.getController();
            controller.setMainController(this);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openOrderHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uvelirkurs/order_history.fxml"));
            
            Stage stage = (Stage) ordersButton.getScene().getWindow();
            Scene scene = new Scene(loader.load(), stage.getWidth(), stage.getHeight());
            
            OrderHistoryController controller = loader.getController();
            controller.setMainController(this);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addProductCard(JSONObject product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/uvelirkurs/product_card.fxml")
            );

            VBox card = loader.load();
            ProductCardController controller = loader.getController();
            controller.setProduct(product);

            productsPane.getChildren().add(card);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
