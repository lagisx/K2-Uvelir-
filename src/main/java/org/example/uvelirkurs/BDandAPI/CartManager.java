package org.example.uvelirkurs.BDandAPI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.uvelirkurs.models.CartItem;
import org.json.JSONArray;
import org.json.JSONObject;

public class CartManager {
    private static CartManager instance;
    private ObservableList<CartItem> cartItems;
    private boolean isLoadingFromDB = false;

    private CartManager() {
        cartItems = FXCollections.observableArrayList();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void loadCartFromDB(int userId) {
        if (isLoadingFromDB) return;
        
        isLoadingFromDB = true;
        SupabaseService.getCartItems(userId).thenAccept(cartData -> {
            Platform.runLater(() -> {
                cartItems.clear();
                
                for (int i = 0; i < cartData.length(); i++) {
                    JSONObject cartItem = cartData.getJSONObject(i);
                    int productId = cartItem.getInt("product_id");
                    
                    JSONObject product = SupabaseService.getProductById(productId);
                    if (product != null) {
                        CartItem item = new CartItem(product);
                        item.setQuantity(cartItem.getInt("quantity"));
                        item.setImageUrl(cartItem.optString("image_url", ""));
                        cartItems.add(item);
                    }
                }
                
                isLoadingFromDB = false;
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            isLoadingFromDB = false;
            return null;
        });
    }

    public void addProduct(JSONObject product, String imageUrl) {
        JSONObject currentUser = SessionManager.getUser();
        if (currentUser == null) return;
        
        int userId = currentUser.getInt("id");
        int productId = product.getInt("id");
        
        for (CartItem item : cartItems) {
            if (item.getProductId() == productId) {
                item.incrementQuantity();
                SupabaseService.saveCartItem(userId, productId, item.getQuantity(), imageUrl);
                return;
            }
        }

        CartItem newItem = new CartItem(product);
        newItem.setImageUrl(imageUrl);
        cartItems.add(newItem);
        
        SupabaseService.saveCartItem(userId, productId, 1, imageUrl);
    }

    public void removeProduct(int productId) {
        JSONObject currentUser = SessionManager.getUser();
        if (currentUser == null) return;
        
        int userId = currentUser.getInt("id");
        cartItems.removeIf(item -> item.getProductId() == productId);
        SupabaseService.removeCartItem(userId, productId);
    }

    public void updateQuantity(int productId, int quantity) {
        JSONObject currentUser = SessionManager.getUser();
        if (currentUser == null) return;
        
        int userId = currentUser.getInt("id");
        
        for (CartItem item : cartItems) {
            if (item.getProductId() == productId) {
                if (quantity <= 0) {
                    removeProduct(productId);
                } else {
                    item.setQuantity(quantity);
                    SupabaseService.saveCartItem(userId, productId, quantity, item.getImageUrl());
                }
                return;
            }
        }
    }

    public void clearCart() {
        JSONObject currentUser = SessionManager.getUser();
        if (currentUser == null) return;
        
        int userId = currentUser.getInt("id");
        cartItems.clear();
        SupabaseService.clearCart(userId);
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    public int getTotalItemsCount() {
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public double getTotalPrice() {
        return cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
}
