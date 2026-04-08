package com.example.flowerboutique.ui.orders;

public class Product {
    private String name;
    private String price;
    private String imageUrl;
    private int quantity;

    public Product(int quantity, String imageUrl, String price, String name) {
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.price = price;
        this.name = name;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
}