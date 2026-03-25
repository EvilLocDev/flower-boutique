package com.example.flowerboutique.ui.cart;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    // Chứa danh sách dữ liệu thực tế
    private List<CartItem> currentList = new ArrayList<>();

    public CartRepository() {
        loadDummyData();
    }

    public List<CartItem> getCartItems() {
        return currentList;
    }

    // Đưa logic Tăng số lượng vào đây
    public void increaseQuantity(CartItem item) {
        item.setQuantity(item.getQuantity() + 1);
    }

    // Đưa logic Giảm số lượng vào đây
    public void decreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        }
    }

    // Đưa logic Xóa vào đây
    public void removeItem(CartItem item) {
        currentList.remove(item);
    }

    // Đưa logic tính tổng tiền vào đây
    public long calculateTotal() {
        long total = 0;
        for (CartItem item : currentList) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    // Tạo dữ liệu giả
    private void loadDummyData() {
        currentList.add(new CartItem("1", "", "Bó hoa Hồng Đỏ",500000, 2, 1));
        currentList.add(new CartItem("2", "", "Lẵng hoa Khai Trương", 1200000, 1, 2));
        currentList.add(new CartItem("3", "", "Bó hoa Hướng Dương", 350000, 3, 3));
    }
}