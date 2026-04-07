package com.example.flowerboutique.db.entities;


import com.google.firebase.Timestamp;
import java.util.List;

public class OrderModel {
    private String documentId;
    private String user_id;
    private String status;
    private String payment_method;
    private String phone_number;
    private Long total_price;
    private Timestamp created_date;
    private Timestamp completed_date;
    private AddressModel address;
    private List<ProductItem> products;

    public OrderModel() {
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getStatus() {
        return status;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public Long getTotal_price() {
        return total_price;
    }

    public Timestamp getCreated_date() {
        return created_date;
    }

    public Timestamp getCompleted_date() {
        return completed_date;
    }

    public AddressModel getAddress() {
        return address;
    }

    public List<ProductItem> getProducts() {
        return products;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setTotal_price(Long total_price) {
        this.total_price = total_price;
    }

    public void setCreated_date(Timestamp created_date) {
        this.created_date = created_date;
    }

    public void setCompleted_date(Timestamp completed_date) {
        this.completed_date = completed_date;
    }

    public void setAddress(AddressModel address) {
        this.address = address;
    }

    public void setProducts(List<ProductItem> products) {
        this.products = products;
    }
}