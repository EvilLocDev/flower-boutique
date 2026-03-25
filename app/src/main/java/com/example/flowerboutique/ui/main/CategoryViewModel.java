package com.example.flowerboutique.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.adapters.CategoryAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel {
    private final List<CategoryAdapter.OverviewCategory> categories = new ArrayList<>();
    private final MutableLiveData<List<CategoryAdapter.OverviewCategory>> liveDataCategories = new MutableLiveData<>(categories);
    private final AppFirebase appFirebase = new AppFirebase();

    public List<CategoryAdapter.OverviewCategory> getCategories() {
        return categories;
    }

    public MutableLiveData<List<CategoryAdapter.OverviewCategory>> getLiveDataCategories() {
        return liveDataCategories;
    }

    public void loadCategories() {
        appFirebase.getCategoriesCollection().get().addOnSuccessListener(snapshot -> {
            categories.clear(); // Xóa dữ liệu cũ trước khi thêm dữ liệu mới
            snapshot.getDocuments().forEach(category -> {
                String name = category.getString("name");
                String thumbnail = category.getString("thumbnail");
                String id = category.getId();
                categories.add(new CategoryAdapter.OverviewCategory(id, name, thumbnail));
            });
            liveDataCategories.setValue(categories);
        });
    }
}
