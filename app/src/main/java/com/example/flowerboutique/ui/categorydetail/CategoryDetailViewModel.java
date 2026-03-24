package com.example.flowerboutique.ui.categorydetail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryDetailViewModel extends ViewModel {
    private AppFirebase appFirebase = new AppFirebase();
    List<OverviewProductAdapter.OverviewProduct> products = new ArrayList<>();
    MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> liveDataProducts = new MutableLiveData<>(products);

    public MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> getLiveDataProducts() {
        return liveDataProducts;
    }

    public List<OverviewProductAdapter.OverviewProduct> getProducts() {
        return products;
    }

    public void loadProducts(String id) {
        appFirebase.getProductsCollection().whereArrayContains("categories", appFirebase.getCategoriesCollection().document(id)).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    task.getResult().getDocuments().forEach(snapshot -> {
                        String name = snapshot.getString("name");
                        String thumbnail = ((List<String>) snapshot.get("image")).get(0);
                        Long price = snapshot.getLong("price") != null ? snapshot.getLong("price") : Long.valueOf(0);
                        String productId = snapshot.getId();
                        Timestamp createdDate = snapshot.getTimestamp("created_date");
                        products.add(new OverviewProductAdapter.OverviewProduct(productId, name, price, thumbnail, createdDate));
                    });
                    liveDataProducts.setValue(products);
                });
    }

    public Task<DocumentSnapshot> loadCategoryName(String id) {
        return appFirebase.getCategoriesCollection().document(id).get();
    }
}
