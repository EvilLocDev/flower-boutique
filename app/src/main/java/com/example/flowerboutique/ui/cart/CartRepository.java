package com.example.flowerboutique.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.flowerboutique.db.dao.CartDAO;
import com.example.flowerboutique.db.entities.CartEntity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CartRepository {

    private final CartDAO cartDAO;
    private final MutableLiveData<Long> totalPriceLiveData = new MutableLiveData<>(0L);
    private final FirebaseFirestore firestore;

    public CartRepository(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
        this.firestore = FirebaseFirestore.getInstance(); // Khởi tạo Firestore
    }

    public LiveData<Long> getTotalPriceLiveData() {
        return totalPriceLiveData;
    }

    // Lấy dữ liệu kết hợp giữa Room (Số lượng) và Firestore (Chi tiết sản phẩm)
    public LiveData<List<CartItem>> getCartItemsWithDetails() {
        MediatorLiveData<List<CartItem>> mediatorLiveData = new MediatorLiveData<>();

        // Lắng nghe sự thay đổi từ giỏ hàng Local (Room)
        LiveData<List<CartEntity>> localCartSource = cartDAO.getAllInCart();

        mediatorLiveData.addSource(localCartSource, entities -> {
            if (entities == null || entities.isEmpty()) {
                mediatorLiveData.setValue(new ArrayList<>());
                totalPriceLiveData.setValue(0L); // Giỏ hàng trống thì tổng tiền = 0
                return;
            }

            // Gọi hàm xử lý lấy dữ liệu từ Firestore
            fetchDetailsFromFirestore(entities, mediatorLiveData);
        });

        return mediatorLiveData;
    }

    // Hàm lấy chi tiết hoa từ Firestore và tính toán
    private void fetchDetailsFromFirestore(List<CartEntity> entities, MutableLiveData<List<CartItem>> resultLiveData) {
        List<CartItem> tempItems = new ArrayList<>();

        // Sử dụng Atomic để đếm an toàn trong môi trường đa luồng (Bất đồng bộ)
        AtomicInteger itemsToFetch = new AtomicInteger(entities.size());
        AtomicLong tempTotal = new AtomicLong(0L);

        for (CartEntity entity : entities) {
            // Truy vấn vào collection "products" với Document ID là product_id
            firestore.collection("products").document(entity.getProduct_id())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Lấy dữ liệu từ Firestore Document
                            String name = documentSnapshot.getString("name");
                            Long price = documentSnapshot.getLong("price");

                            // Xử lý trường "image" vì nó là một Array trên Firestore
                            List<String> images = (List<String>) documentSnapshot.get("image");
                            String imageUrl = "";
                            if (images != null && !images.isEmpty()) {
                                imageUrl = images.get(0); // Lấy link ảnh đầu tiên trong mảng
                            }

                            // Xử lý null an toàn trong trường hợp thiếu field trên Firestore
                            if (price == null) price = 0L;
                            if (name == null) name = "Sản phẩm không xác định";

                            // Tạo đối tượng CartItem để hiển thị lên UI
                            CartItem item = new CartItem(
                                    entity.getProduct_id(),
                                    imageUrl,
                                    name,
                                    price,
                                    entity.getQuantity(),
                                    entity.getId()
                            );
                            tempItems.add(item);

                            // Cộng dồn tổng tiền: Giá * Số lượng
                            tempTotal.addAndGet(price * entity.getQuantity());
                        }

                        // Kiểm tra nếu đã tải xong item cuối cùng trong vòng lặp
                        if (itemsToFetch.decrementAndGet() == 0) {
                            resultLiveData.postValue(tempItems);
                            totalPriceLiveData.postValue(tempTotal.get()); // Cập nhật tổng tiền
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý khi có lỗi (VD: rớt mạng) - Vẫn phải đếm lùi để không bị treo logic
                        if (itemsToFetch.decrementAndGet() == 0) {
                            resultLiveData.postValue(tempItems);
                            totalPriceLiveData.postValue(tempTotal.get());
                        }
                    });
        }
    }

    // --- Các hàm tương tác Database Local (Room) ---
    public void increaseQuantity(String productId) {
        new Thread(() -> cartDAO.modifyQuantity(1, productId)).start();
    }

    public void decreaseQuantity(String productId) {
        new Thread(() -> cartDAO.decreaseQuantity(productId)).start();
    }

    public void removeItem(String productId) {
        new Thread(() -> cartDAO.deleteItem(productId)).start();
    }
}