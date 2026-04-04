package com.example.flowerboutique.ui.main_home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.Timestamp;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProductSearchViewModel extends ViewModel {

    private final MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> filteredProducts = new MutableLiveData<>();
    private final List<OverviewProductAdapter.OverviewProduct> allProducts = new ArrayList<>();
    private final AppFirebase appFirebase = new AppFirebase();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<List<OverviewProductAdapter.OverviewProduct>> getFilteredProducts() {
        return filteredProducts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadAllProducts() {
        isLoading.setValue(true);
        appFirebase.getProductsCollection().get()
                .addOnSuccessListener(result -> {
                    allProducts.clear();
                    result.getDocuments().forEach(snapshot -> {
                        try {
                            String name = snapshot.getString("name");
                            Object imgObj = snapshot.get("image");
                            String thumbnail = "";
                            if (imgObj instanceof List) {
                                List<String> images = (List<String>) imgObj;
                                if (!images.isEmpty()) thumbnail = images.get(0);
                            }
                            Long price = snapshot.getLong("price") != null ? snapshot.getLong("price") : 0L;
                            String id = snapshot.getId();
                            Timestamp createdDate = snapshot.getTimestamp("created_date");
                            allProducts.add(new OverviewProductAdapter.OverviewProduct(id, name, price, thumbnail, createdDate));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> isLoading.setValue(false));
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredProducts.setValue(new ArrayList<>());
            return;
        }
        
        String normalizedQuery = removeAccent(query);
        List<OverviewProductAdapter.OverviewProduct> result = new ArrayList<>();
        for (OverviewProductAdapter.OverviewProduct product : allProducts) {
            if (removeAccent(product.getName()).contains(normalizedQuery)) {
                result.add(product);
            }
        }
        filteredProducts.setValue(result);
    }

    private String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d").replace("Đ", "d");
    }
}
