package com.example.flowerboutique.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.Locale;

import com.example.flowerboutique.R; // Nhớ đổi đúng package
import com.example.flowerboutique.ui.makeorder.MakeOrderActivity;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private TextView tvTotalCost;
    private Button btnProceedToCheckout;

    private CartAdapter adapter;
    private CartViewModel viewModel;
    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(CartViewModel.class);

        // 2. Lắng nghe dữ liệu (Observe Data)
        observeViewModel();

        // 3. Xử lý các nút bấm ngoài danh sách
        setupClickEvents();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter();
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(adapter);

        // Chuyển tiếp các thao tác click lên ViewModel xử lý
        adapter.setActionListener(new CartAdapter.CartItemActionListener() {
            @Override
            public void onIncrease(CartItem item) {
                viewModel.increaseQuantity(item);
            }

            @Override
            public void onDecrease(CartItem item) {
                viewModel.decreaseQuantity(item);
            }

            @Override
            public void onRemove(CartItem item) {
                viewModel.removeItem(item);
            }
        });
    }

    private void observeViewModel() {
        // Lắng nghe danh sách Giỏ hàng
        viewModel.getCartListLiveData().observe(this, cartItems -> {
            adapter.setCartItems(cartItems);

            // Nếu giỏ hàng trống thì khóa nút Thanh toán
            btnProceedToCheckout.setEnabled(cartItems != null && !cartItems.isEmpty());
        });

        // Lắng nghe Tổng tiền
        viewModel.getTotalPriceLiveData().observe(this, totalPrice -> {
            tvTotalCost.setText(numberFormat.format(totalPrice));
        });
    }

    private void setupClickEvents() {
        btnProceedToCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, MakeOrderActivity.class);
            startActivity(intent);
        });
    }
}