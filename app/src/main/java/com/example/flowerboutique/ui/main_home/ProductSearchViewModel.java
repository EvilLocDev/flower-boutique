package com.example.flowerboutique.ui.main_home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.Timestamp;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class ProductSearchViewModel extends ViewModel {

    public enum SortType {
        NONE, PRICE_ASC, PRICE_DESC
    }

    private final MutableLiveData<List<OverviewProductAdapter.OverviewProduct>> filteredProducts = new MutableLiveData<>();
    private final List<OverviewProductAdapter.OverviewProduct> allProducts = new ArrayList<>();
    private final AppFirebase appFirebase = new AppFirebase();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private String currentQuery = "";
    private Long currentMinPrice = null;
    private Long currentMaxPrice = null;
    private SortType currentSortType = SortType.NONE;

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
                    applyFilters();
                })
                .addOnFailureListener(e -> isLoading.setValue(false));
    }

    public void setQuery(String query) {
        this.currentQuery = query;
        applyFilters();
    }

    public void setPriceFilter(Long minPrice, Long maxPrice) {
        this.currentMinPrice = minPrice;
        this.currentMaxPrice = maxPrice;
        applyFilters();
    }

    public void setSortType(SortType sortType) {
        this.currentSortType = sortType;
        applyFilters();
    }

    private void applyFilters() {
        List<OverviewProductAdapter.OverviewProduct> result = new ArrayList<>();
        String normalizedQuery = removeAccent(currentQuery);

        for (OverviewProductAdapter.OverviewProduct product : allProducts) {
            boolean matchesQuery = currentQuery.isEmpty() || removeAccent(product.getName()).contains(normalizedQuery);
            boolean matchesMinPrice = currentMinPrice == null || product.getPrice() >= currentMinPrice;
            boolean matchesMaxPrice = currentMaxPrice == null || product.getPrice() <= currentMaxPrice;

            if (matchesQuery && matchesMinPrice && matchesMaxPrice) {
                result.add(product);
            }
        }

        // Sắp xếp
        if (currentSortType == SortType.PRICE_ASC) {
            Collections.sort(result, Comparator.comparingLong(OverviewProductAdapter.OverviewProduct::getPrice));
        } else if (currentSortType == SortType.PRICE_DESC) {
            Collections.sort(result, (p1, p2) -> Long.compare(p2.getPrice(), p1.getPrice()));
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
