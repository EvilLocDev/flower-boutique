package com.example.flowerboutique.ui.main_home;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductSearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageButton btnBack, btnClear;
    private RecyclerView rvResults;
    private TextView tvNoResults;
    private ProgressBar pbLoading;

    private OverviewProductAdapter adapter;
    private ProductSearchViewModel viewModel;
    private final List<OverviewProductAdapter.OverviewProduct> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_product_search);

        viewModel = new ViewModelProvider(this).get(ProductSearchViewModel.class);

        initViews();
        setupRecyclerView();
        observeViewModel();
        setupEvents();

        viewModel.loadAllProducts();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnBack = findViewById(R.id.btn_back);
        btnClear = findViewById(R.id.btn_clear);
        rvResults = findViewById(R.id.rv_search_results);
        tvNoResults = findViewById(R.id.tv_no_results);
        pbLoading = findViewById(R.id.pb_loading);
    }

    private void setupRecyclerView() {
        adapter = new OverviewProductAdapter(displayList);
        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        
        int spacing = (int) (12 * getResources().getDisplayMetrics().density);
        rvResults.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
        
        rvResults.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getFilteredProducts().observe(this, products -> {
            displayList.clear();
            if (products != null) {
                displayList.addAll(products);
            }
            adapter.notifyDataSetChanged();
            
            // Hiển thị thông báo nếu không có kết quả (chỉ khi đã nhập text)
            String query = etSearch.getText().toString();
            if (!query.isEmpty() && displayList.isEmpty()) {
                tvNoResults.setVisibility(View.VISIBLE);
            } else {
                tvNoResults.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
        btnClear.setOnClickListener(v -> etSearch.setText(""));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                btnClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                viewModel.filter(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) outRect.top = spacing;
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) outRect.top = spacing;
            }
        }
    }
}
