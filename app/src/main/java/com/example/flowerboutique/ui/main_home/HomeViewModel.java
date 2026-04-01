package com.example.flowerboutique.ui.main_home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final ArrayList<OverviewProductAdapter.OverviewProduct> products = new ArrayList<>();
    private final MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> liveDataProducts = new MutableLiveData<>(products);
    private final AppFirebase appFirebase = new AppFirebase();

    public Task<QuerySnapshot> loadProducts() {
        // Lấy sản phẩm mà không dùng orderBy để tránh lỗi thiếu Index trên Firestore
        return appFirebase.getProductsCollection()
                .limit(30) // Lấy 30 sản phẩm đầu tiên
                .get()
                .addOnSuccessListener(result -> {
                    products.clear();
                    result.getDocuments().forEach(snapshot -> {
                        try {
                            String name = snapshot.getString("name");
                            
                            // Ép kiểu mảng ảnh giống CategoryDetailViewModel
                            List<String> images = (List<String>) snapshot.get("image");
                            String thumbnail = (images != null && !images.isEmpty()) ? images.get(0) : "";
                            
                            Long price = snapshot.getLong("price") != null ? snapshot.getLong("price") : 0L;
                            String id = snapshot.getId();
                            Timestamp createdDate = snapshot.getTimestamp("created_date");
                            
                            products.add(new OverviewProductAdapter.OverviewProduct(id, name, price, thumbnail, createdDate));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    liveDataProducts.setValue(products);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    public MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> getLiveDataProducts() {
        return liveDataProducts;
    }

    public ArrayList<OverviewProductAdapter.OverviewProduct> getProducts() {
        return products;
    }
}
