package com.example.flowerboutique.ui.main_home;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.MainFragmentHomeBinding;
import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    MainFragmentHomeBinding binding;
    List<OverviewProductAdapter.OverviewProduct> products;
    OverviewProductAdapter productsAdapter;
    HomeViewModel viewModel;
    boolean isLoading = false;

    public HomeFragment() {
        products = new ArrayList<>();
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.loadProducts();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MainFragmentHomeBinding.inflate(inflater, container, false);
        initFragment();
        return binding.getRoot();
    }


    static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position <= 0) return; // Header (position 0) không cần padding đặc biệt này
            
            // Các sản phẩm bắt đầu từ position 1
            if (position % 2 != 0) { // Cột trái (1, 3, 5...)
                outRect.right = 15;
            } else { // Cột phải (2, 4, 6...)
                outRect.left = 15;
            }
            outRect.bottom = 30;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initFragment() {
        products = viewModel.getProducts();
        productsAdapter = new OverviewProductAdapter(products);
        
        HomeLinearAdapter homeLinearAdapter = new HomeLinearAdapter();
        ConcatAdapter concatAdapter = new ConcatAdapter(homeLinearAdapter, productsAdapter);
        
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Vị trí 0 là HomeLinearAdapter (Header) -> Chiếm 2 cột
                return (position == 0) ? 2 : 1;
            }
        });

        binding.recycleView.setAdapter(concatAdapter);
        binding.recycleView.setLayoutManager(gridLayoutManager);
        binding.recycleView.addItemDecoration(new SpaceItemDecoration());
        binding.recycleView.addOnScrollListener(new OnScrollRecyclerView());
        
        // Sử dụng getViewLifecycleOwner() để quan sát dữ liệu an toàn
        viewModel.getLiveDataProducts().observe(getViewLifecycleOwner(), a -> {
            productsAdapter.notifyDataSetChanged();
        });
    }

    class OnScrollRecyclerView extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

            if (!isLoading && layoutManager != null && layoutManager.findLastVisibleItemPosition() >= products.size() - 2) {
                isLoading = true;
                Task<QuerySnapshot> task = viewModel.loadProducts();
                if (task != null) {
                    task.addOnSuccessListener(v -> isLoading = false)
                        .addOnFailureListener(e -> isLoading = false);
                } else {
                    isLoading = false;
                }
            }
        }
    }
}
