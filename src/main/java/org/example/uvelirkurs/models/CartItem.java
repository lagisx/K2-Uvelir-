package org.example.uvelirkurs.models;

import org.json.JSONObject;

public class CartItem {
    private int productId;
    private String name;
    private String material;
    private double price;
    private String imageUrl;
    private int quantity;

    public CartItem(JSONObject product) {
        this.productId = product.getInt("id");
        this.name = product.optString("name", "Без имени");
        this.material = product.optString("material", "-");
        this.price = product.optDouble("price", 0);
        this.imageUrl = ""; // Будет установлено отдельно
        this.quantity = 1;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getMaterial() { return material; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public void incrementQuantity() { this.quantity++; }
    public void decrementQuantity() { 
        if (this.quantity > 1) this.quantity--; 
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}
