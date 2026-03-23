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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.loadProducts();
        products = viewModel.getProducts();
        productsAdapter = new OverviewProductAdapter(products);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        {
            binding = MainFragmentHomeBinding.inflate(inflater, container, false);
            View view = binding.getRoot();
            initFragment();
            initEvent();
            return view;
        }
    }


    static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position == 0) return;
            if (position % 2 == 0) outRect.left = 25;
            else outRect.right = 25;

            if (position <= state.getItemCount() - 3) outRect.bottom = 40;
        }
    }

    private void initEvent() {
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initFragment() {
        RecyclerView recyclerView = binding.getRoot().findViewById(R.id.recycle_view);
        HomeLinearAdapter homeLinearAdapter = new HomeLinearAdapter();
        ConcatAdapter concatAdapter = new ConcatAdapter(homeLinearAdapter, productsAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this.getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setAdapter(concatAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new SpaceItemDecoration());
        recyclerView.addOnScrollListener(new OnScrollRecyclerView());
        viewModel.getLiveDataProducts().observeForever(a -> {
            productsAdapter.notifyDataSetChanged();
        });
    }

    class OnScrollRecyclerView extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

            if (!isLoading && gridLayoutManager != null && gridLayoutManager.findLastVisibleItemPosition() == products.size() - 2) {
                isLoading = true;
                Task<QuerySnapshot> loading = viewModel.loadProducts();
                if (loading != null) loading.addOnSuccessListener(v -> {
                    isLoading = false;
                });
                else isLoading = false;
            }
        }
    }
}