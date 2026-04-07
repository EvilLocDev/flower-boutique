package com.example.flowerboutique.db.entities;
import com.google.firebase.firestore.DocumentReference;

public class ProductItem {
    private String name;
    private DocumentReference product;
    private Long quantity;
    private Long unitPrice;

    public ProductItem() {
    }

    public String getName() {
        return name;
    }

    public DocumentReference getProduct() {
        return product;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getUnitPrice() {
        return unitPrice;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProduct(DocumentReference product) {
        this.product = product;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
    }
}