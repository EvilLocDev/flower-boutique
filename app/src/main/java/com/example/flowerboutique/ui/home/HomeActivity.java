package com.example.flowerboutique.ui.home;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flowerboutique.databinding.ActivityHomeBinding;
import com.example.flowerboutique.db.entities.CategoryEntity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private CategoryAdapter adapter;
    private List<CategoryEntity> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new CategoryAdapter(categoryList);
        if (binding.rvCategories != null) {
            binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            binding.rvCategories.setAdapter(adapter);
        }

        fetchDataFromLoc();
    }

    private void fetchDataFromLoc() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                categoryList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    CategoryEntity item = doc.toObject(CategoryEntity.class);
                    categoryList.add(item);
                }
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {
            Log.e("FirebaseError", "Lỗi lấy dữ liệu: " + e.getMessage());
        });
    }
}