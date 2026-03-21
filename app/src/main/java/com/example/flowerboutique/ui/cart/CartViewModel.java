package com.example.flowerboutique.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {

    // Danh sách giỏ hàng
    private MutableLiveData<List<CartItem>> cartListLiveData = new MutableLiveData<>();
    // Tổng tiền
    private MutableLiveData<Long> totalPriceLiveData = new MutableLiveData<>(0L);

    private List<CartItem> currentList = new ArrayList<>();

    public CartViewModel() {
        loadDummyData(); // Tạm thời load data giả để test
    }

    public LiveData<List<CartItem>> getCartListLiveData() {
        return cartListLiveData;
    }

    public LiveData<Long> getTotalPriceLiveData() {
        return totalPriceLiveData;
    }

    // Tạo dữ liệu giả
    private void loadDummyData() {
        currentList.add(new CartItem("1", "", "Bó hoa Hồng Đỏ",500000, 2, 1));
        currentList.add(new CartItem("2", "", "Lẵng hoa Khai Trương", 1200000, 1, 2));
        currentList.add(new CartItem("3", "", "Bó hoa Hướng Dương", 350000, 3, 3));

        cartListLiveData.setValue(currentList);
        calculateTotal();
    }

    // Logic Tăng số lượng
    public void increaseQuantity(CartItem item) {
        item.setQuantity(item.getQuantity() + 1);
        cartListLiveData.setValue(currentList); // Cập nhật lại UI
        calculateTotal();
    }

    // Logic Giảm số lượng
    public void decreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartListLiveData.setValue(currentList);
            calculateTotal();
        }
    }

    // Logic Xóa sản phẩm
    public void removeItem(CartItem item) {
        currentList.remove(item);
        cartListLiveData.setValue(currentList);
        calculateTotal();
    }

    // Tính tổng tiền
    private void calculateTotal() {
        long total = 0;
        for (CartItem item : currentList) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceLiveData.setValue(total);
    }
}
