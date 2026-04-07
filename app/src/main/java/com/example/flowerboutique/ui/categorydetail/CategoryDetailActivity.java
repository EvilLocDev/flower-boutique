package com.example.flowerboutique.ui.categorydetail;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityCategoryDetailBinding;
import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;

public class CategoryDetailActivity extends AppCompatActivity {

    ActivityCategoryDetailBinding binding;
    CategoryDetailViewModel viewModel;
    OverviewProductAdapter adapter;
    String id;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryDetailBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(CategoryDetailViewModel.class);
        adapter = new OverviewProductAdapter(viewModel.getProducts());
        id = getIntent().getStringExtra("id");
        if (id == null || id.isEmpty()) finish();

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.products.setLayoutManager(new GridLayoutManager(this, 2));
        binding.products.setAdapter(adapter);
        binding.products.addItemDecoration(new Divider());
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
        viewModel.getLiveDataProducts().observeForever(observer -> {
            adapter.notifyDataSetChanged();
        });
        viewModel.loadProducts(id);
        viewModel.loadCategoryName(id).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                binding.categoryName.setText(task.getResult().getString("name"));
            } else {
                finish();
            }
        });
    }

    public static class Divider extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position % 2 == 0) {
                outRect.right = 25;
            } else {
                outRect.left = 25;
            }
        }
    }
}