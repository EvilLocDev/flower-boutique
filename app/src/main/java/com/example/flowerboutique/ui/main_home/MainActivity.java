package com.example.flowerboutique.ui.main_home;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    BoutiqueApplication application;
    MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = BoutiqueApplication.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MainTabAdapter pagerAdapter = new MainTabAdapter(this);
        binding.pager.registerOnPageChangeCallback(new MainViewModel.PageChange(this::onPageChange));
        binding.pager.setAdapter(pagerAdapter);

        binding.bottomNavigation.setOnItemSelectedListener(this::onSelectHeader);
    }

    private void onPageChange(int position) {
        switch (position) {
            case 0:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                break;
            case 1:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_categories);
                break;
            case 2:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_cart);
                break;
            case 3:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
                break;
        }
    }

    private boolean onSelectHeader(MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            binding.pager.setCurrentItem(0);
        } else if (item.getItemId() == R.id.nav_categories) {
            binding.pager.setCurrentItem(1);
        } else if (item.getItemId() == R.id.nav_cart) {
            binding.pager.setCurrentItem(2);
        } else if (item.getItemId() == R.id.nav_profile) {
            binding.pager.setCurrentItem(3);
        }
        return true;
    }
}
