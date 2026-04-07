package com.example.flowerboutique.ui.main_home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.MainFragmentCategoryBinding;
import com.example.flowerboutique.ui.main.CategoryViewModel;
import com.example.flowerboutique.utils.adapters.CategoryAdapter;

import java.util.List;


public class CategoryFragment extends Fragment {

    MainFragmentCategoryBinding binding;
    CategoryViewModel viewModel;
    List<CategoryAdapter.OverviewCategory> categories;
    CategoryAdapter categoryAdapter;

    public CategoryFragment() {
    }

    public static CategoryFragment newInstance() {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categories = viewModel.getCategories();
        categoryAdapter = new CategoryAdapter(categories);
        viewModel.loadCategories();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = MainFragmentCategoryBinding.inflate(inflater, container, false);
        binding.categoryList.setAdapter(categoryAdapter);
        viewModel.getLiveDataCategories().observe(getViewLifecycleOwner(), a -> {
            categoryAdapter.notifyDataSetChanged();
        });
        return binding.getRoot();
    }
}