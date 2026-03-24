package com.example.flowerboutique.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final ArrayList<OverviewProductAdapter.OverviewProduct> products = new ArrayList<>();
    private final MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> liveDataProducts = new MutableLiveData<>(products);
    private boolean isEndOfProductCollection = false;
    private final AppFirebase appFirebase = new AppFirebase();

    public Task<QuerySnapshot> loadProducts() {
        if (isEndOfProductCollection) return null;
        Query query = appFirebase.getProductsCollection().where(Filter.equalTo("status", true)).orderBy("created_date", Query.Direction.DESCENDING).orderBy(FieldPath.documentId(), Query.Direction.ASCENDING).limit(10);
        if (!products.isEmpty()) {
            OverviewProductAdapter.OverviewProduct lastProduct = products.get(products.size() - 1);
            query = query.where(Filter.or(
                    Filter.and(Filter.equalTo("created_date", lastProduct.getCreatedDate()), Filter.greaterThan(FieldPath.documentId(), lastProduct.getId())),
                    Filter.lessThan("created_date", lastProduct.getCreatedDate())
            ));
        }

        return query.get().addOnSuccessListener(result -> {
            if (result.size() < 10) isEndOfProductCollection = true;
            result.getDocuments().forEach(snapshot -> {
                String name = snapshot.getString("name");
                String thumbnail = ((List<String>) snapshot.get("image")).get(0);
                Long price = snapshot.getLong("price") != null ? snapshot.getLong("price") : Long.valueOf(0);
                String id = snapshot.getId();
                Timestamp createdDate = snapshot.getTimestamp("created_date");
                products.add(new OverviewProductAdapter.OverviewProduct(id, name, price, thumbnail, createdDate));
            });
            liveDataProducts.setValue(products);
        });
    }

    public MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> getLiveDataProducts() {
        return liveDataProducts;
    }

    public ArrayList<OverviewProductAdapter.OverviewProduct> getProducts() {
        return products;
    }
}

