package com.example.flowerboutique.ui.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.flowerboutique.db.RoomDB;
import com.example.flowerboutique.db.dao.CartDAO;

import java.util.List;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;
    private final LiveData<List<CartItem>> cartListLiveData;

    public CartViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo Room và truyền DAO vào Repository
        CartDAO cartDAO = RoomDB.getInstance(application).cartDAO();
        repository = new CartRepository(cartDAO);

        // Lấy LiveData đã được Repository xử lý (Gộp Room + Firestore)
        cartListLiveData = repository.getCartItemsWithDetails();
    }

    // Fragment/Activity sẽ observe cái này để vẽ RecyclerView
    public LiveData<List<CartItem>> getCartListLiveData() {
        return cartListLiveData;
    }

    // Fragment/Activity sẽ observe cái này để hiển thị Text tổng tiền
    public LiveData<Long> getTotalPriceLiveData() {
        return repository.getTotalPriceLiveData();
    }

    // Các hàm tương tác từ UI đẩy xuống
    public void increaseQuantity(CartItem item) {
        repository.increaseQuantity(item.getId()); // getId() ở đây trả về product_id
    }

    public void decreaseQuantity(CartItem item) {
        repository.decreaseQuantity(item.getId());
    }

    public void removeItem(CartItem item) {
        repository.removeItem(item.getId());
    }
}