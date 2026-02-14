package org.example.uvelirkurs.models;

public class ProductData {
    private final int id;
    private final String name;
    private final String material;
    private final double price;
    private final int stockQuantity;

    public ProductData(int id, String name, String material, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getMaterial() { return material; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
}