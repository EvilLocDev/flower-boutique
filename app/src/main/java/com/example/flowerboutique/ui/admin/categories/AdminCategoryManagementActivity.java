package com.example.flowerboutique.ui.admin.categories;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityAdminCategoryManagementBinding;

public class AdminCategoryManagementActivity extends AppCompatActivity {
    ActivityAdminCategoryManagementBinding binding;
    AdminCategoryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminCategoryManagementBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AdminCategoryViewModel.class);
        viewModel.setContext(this);

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(v -> finish());
        binding.categoriesList.setLayoutManager(new LinearLayoutManager(this));
        binding.categoriesList.setAdapter(viewModel.getAdapter());
        binding.addCategoryBtn.setOnClickListener(v -> {
            if (viewModel.getActivityResultLauncher() != null)
                viewModel.getActivityResultLauncher().launch(new Intent(this, AdminCategoryDetailActivity.class));
            else
                startActivity(new Intent(this, AdminCategoryDetailActivity.class));
        });
    }
}
