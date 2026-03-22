package com.example.flowerboutique.ui.makeorder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.ui.cart.CartItem; // Import model CartItem của bạn vào đây

public class MakeOrderViewModel extends ViewModel {
    // Kéo dữ liệu trực tiếp từ Giỏ hàng (Room DB) sang để chuẩn bị thanh toán
    private final MutableLiveData<List<CartItem>> orderItemsLiveData = BoutiqueApplication.getInstance().getCartItemsLiveData();

    public MutableLiveData<List<CartItem>> getOrderItemsLiveData() {
        return orderItemsLiveData;
    }
}
