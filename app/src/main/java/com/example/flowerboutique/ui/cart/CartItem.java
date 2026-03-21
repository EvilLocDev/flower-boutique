package com.example.flowerboutique.ui.cart;

public class CartItem {
    private String id;
    private String imageUrl;
    private String name;
    private long price;
    private int quantity;
    private int cartEntityId;

    public CartItem(String id, String imageUrl, String name, long price, int quantity, int cartEntityId) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.cartEntityId = cartEntityId;
    }

    // Các hàm Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getCartEntityId() { return cartEntityId; }
    public void setCartEntityId(int cartEntityId) { this.cartEntityId = cartEntityId; }
}