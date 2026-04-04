package com.example.flowerboutique.ui.main_home;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.example.flowerboutique.utils.adapters.OverviewProductAdapter;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.Timestamp;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProductSearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageButton btnBack, btnClear;
    private RecyclerView rvResults;
    private TextView tvNoResults;
    private ProgressBar pbLoading;

    private OverviewProductAdapter adapter;
    private List<OverviewProductAdapter.OverviewProduct> allProducts = new ArrayList<>();
    private List<OverviewProductAdapter.OverviewProduct> filteredList = new ArrayList<>();

    private final AppFirebase appFirebase = new AppFirebase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.main_product_search);
            initViews();
            setupRecyclerView();
            loadAllProducts();
            setupEvents();
        } catch (Exception e) {
            Log.e("SEARCH_ERROR", "Lỗi khởi tạo: " + e.getMessage());
            finish();
        }
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
        adapter = new OverviewProductAdapter(filteredList);
        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Thêm khoảng cách 12dp giữa các item để trông đẹp hơn
        int spacing = (int) (8 * getResources().getDisplayMetrics().density);
        rvResults.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
        
        rvResults.setAdapter(adapter);
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
                filterProducts(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllProducts() {
        pbLoading.setVisibility(View.VISIBLE);
        appFirebase.getProductsCollection().get()
                .addOnSuccessListener(result -> {
                    allProducts.clear();
                    result.getDocuments().forEach(snapshot -> {
                        try {
                            String name = snapshot.getString("name");
                            
                            // Xử lý ảnh an toàn tránh crash do ép kiểu
                            Object imgObj = snapshot.get("image");
                            String thumbnail = "";
                            if (imgObj instanceof List) {
                                List<String> images = (List<String>) imgObj;
                                if (!images.isEmpty()) thumbnail = images.get(0);
                            }
                            
                            Long price = snapshot.getLong("price") != null ? snapshot.getLong("price") : 0L;
                            String id = snapshot.getId();
                            Timestamp createdDate = snapshot.getTimestamp("created_date");
                            
                            allProducts.add(new OverviewProductAdapter.OverviewProduct(id, name, price, thumbnail, createdDate));
                        } catch (Exception e) {
                            Log.e("SEARCH_ERROR", "Lỗi parse product: " + e.getMessage());
                        }
                    });
                    pbLoading.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    Log.e("SEARCH_ERROR", "Lỗi tải Firestore: " + e.getMessage());
                });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.trim().isEmpty()) {
            tvNoResults.setVisibility(View.GONE);
        } else {
            String normalizedQuery = removeAccent(query);
            for (OverviewProductAdapter.OverviewProduct product : allProducts) {
                if (removeAccent(product.getName()).contains(normalizedQuery)) {
                    filteredList.add(product);
                }
            }
            tvNoResults.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    public String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d").replace("Đ", "d");
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
