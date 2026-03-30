package com.example.flowerboutique.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {

    private MutableLiveData<List<CartItem>> cartListLiveData = new MutableLiveData<>();
    private MutableLiveData<Long> totalPriceLiveData = new MutableLiveData<>(0L);

    // Gọi file chứa logic vào đây
    private CartRepository repository;

    public CartViewModel() {
        repository = new CartRepository();
        updateLiveData();
    }

    public LiveData<List<CartItem>> getCartListLiveData() { return cartListLiveData; }
    public LiveData<Long> getTotalPriceLiveData() { return totalPriceLiveData; }

    public void increaseQuantity(CartItem item) {
        repository.increaseQuantity(item); // Nhờ Repository xử lý logic
        updateLiveData(); // Cập nhật lại UI
    }

    public void decreaseQuantity(CartItem item) {
        repository.decreaseQuantity(item);
        updateLiveData();
    }

    public void removeItem(CartItem item) {
        repository.removeItem(item);
        updateLiveData();
    }

    // Hàm đồng bộ dữ liệu từ Repository lên LiveData
    private void updateLiveData() {
        cartListLiveData.setValue(new ArrayList<>(repository.getCartItems()));
        totalPriceLiveData.setValue(repository.calculateTotal());
    }
}