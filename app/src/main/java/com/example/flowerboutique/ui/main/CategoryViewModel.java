package com.example.flowerboutique.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.adapters.CategoryAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel {
    private final List<CategoryAdapter.OverviewCategory> categories = new ArrayList<>();
    private final MutableLiveData<List<CategoryAdapter.OverviewCategory>> liveDataCategories = new MutableLiveData<>(categories);
    private final AppFirebase appFirebase = new AppFirebase();
    private ListenerRegistration categoriesListener;

    public List<CategoryAdapter.OverviewCategory> getCategories() {
        return categories;
    }

    public MutableLiveData<List<CategoryAdapter.OverviewCategory>> getLiveDataCategories() {
        return liveDataCategories;
    }

    public void loadCategories() {
        if (categoriesListener != null) return; // Tránh tạo nhiều listener

        categoriesListener = appFirebase.getCategoriesCollection().addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null) return;

            categories.clear();
            snapshot.getDocuments().forEach(category -> {
                String name = category.getString("name");
                String thumbnail = category.getString("thumbnail");
                String id = category.getId();
                categories.add(new CategoryAdapter.OverviewCategory(id, name, thumbnail));
            });
            liveDataCategories.setValue(categories);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (categoriesListener != null) {
            categoriesListener.remove();
        }
    }
}