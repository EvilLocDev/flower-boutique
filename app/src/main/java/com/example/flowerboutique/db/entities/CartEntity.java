package com.example.flowerboutique.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_table")
public class CartEntity {

    @PrimaryKey(autoGenerate = true)
    private int id; // ID tự tăng của dòng trong giỏ hàng

    @NonNull
    private String product_id; // ID của hoa (Lấy từ Firebase)

    private int quantity; // Số lượng khách đặt

    // Constructor
    public CartEntity(){};
    public CartEntity(@NonNull String product_id, int quantity) {
        this.product_id = product_id;
        this.quantity = quantity;
    }

    // --- Các Getters và Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getProduct_id() { return product_id; }
    public void setProduct_id(@NonNull String product_id) { this.product_id = product_id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}