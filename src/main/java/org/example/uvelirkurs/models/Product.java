package org.example.uvelirkurs.models;
public class Product {
    private int id;
    private int categoryId;
    private int supplierId;
    private String name;
    private String description;
    private String material;
    private String purity;
    private double weight;
    private String size;
    private double price;
    private double costPrice;
    private int stockQuantity;
    private String collection;

    public Product(int id, String name, String material, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getMaterial() { return material; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
}
