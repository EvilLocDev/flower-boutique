package com.example.flowerboutique.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityAdminBinding;
import com.example.flowerboutique.ui.admin.categories.AdminCategoryManagementActivity;
import com.example.flowerboutique.ui.admin.dashboard.AdminDashboardActivity;
import com.example.flowerboutique.ui.admin.orders.OrderManagementActivity;
import com.example.flowerboutique.ui.admin.products.AdminProductManagementActivity;
import com.example.flowerboutique.ui.admin.users.AdminUserManagementActivity;

public class AdminActivity extends AppCompatActivity {
    ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(v -> {
            finish();
        });

        binding.orderManagementBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderManagementActivity.class);
            startActivity(intent);
        });

        binding.productManagementBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProductManagementActivity.class);
            startActivity(intent);
        });

        binding.userManagementBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUserManagementActivity.class));
        });

        binding.categoryManagementBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminCategoryManagementActivity.class));
        });

        binding.dashboardAdminBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        });
    }
}