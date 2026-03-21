package com.example.flowerboutique.ui.cart;

public class CartItem {
    private String id;
    private String name;
    private long price;
    private int quantity;

    public CartItem(String id, String name, long price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Các hàm Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}