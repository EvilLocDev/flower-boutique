package com.example.flowerboutique.ui.main_home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.cart.CartAdapter;
import com.example.flowerboutique.ui.cart.CartViewModel;
import com.example.flowerboutique.ui.makeorder.MakeOrderActivity;
import com.example.flowerboutique.ui.profile.LoginRegisterFragment;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;

public class CartFragment extends Fragment {

    private BoutiqueApplication application = BoutiqueApplication.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Sử dụng layout container để hoán đổi fragment
        return inflater.inflate(R.layout.main_fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        renderFragment(application.getAppFirebase().getFirebaseAuth().getCurrentUser());
    }

    private void renderFragment(FirebaseUser firebaseUser) {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (firebaseUser == null) {
            // Nếu chưa đăng nhập, hiển thị trang Login/Register
            fragmentManager.beginTransaction()
                    .replace(R.id.main_cart_container, LoginRegisterFragment.class, null)
                    .commit();
        } else {
            // Nếu đã đăng nhập, hiển thị nội dung giỏ hàng
            fragmentManager.beginTransaction()
                    .replace(R.id.main_cart_container, CartContentFragment.class, null)
                    .commit();
        }
    }


    public static class CartContentFragment extends Fragment {
        private RecyclerView rvCartItems;
        private TextView tvTotalCost;
        private Button btnProceedToCheckout;

        private CartAdapter adapter;
        private CartViewModel viewModel;
        private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_cart_content, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            initViews(view);
            setupRecyclerView();

            // Khởi tạo ViewModel (sử dụng getActivity() để chia sẻ scope nếu cần, hoặc Fragment này tự quản lý)
            viewModel = new ViewModelProvider(this).get(CartViewModel.class);

            observeViewModel();
            setupClickEvents();
        }

        private void initViews(View view) {
            rvCartItems = view.findViewById(R.id.rvCartItems);
            tvTotalCost = view.findViewById(R.id.tvTotalCost);
            btnProceedToCheckout = view.findViewById(R.id.btnProceedToCheckout);
        }

        private void setupRecyclerView() {
            adapter = new CartAdapter();
            rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCartItems.setAdapter(adapter);

            adapter.setActionListener(new CartAdapter.CartItemActionListener() {
                @Override
                public void onIncrease(com.example.flowerboutique.ui.cart.CartItem item) {
                    viewModel.increaseQuantity(item);
                }

                @Override
                public void onDecrease(com.example.flowerboutique.ui.cart.CartItem item) {
                    viewModel.decreaseQuantity(item);
                }

                @Override
                public void onRemove(com.example.flowerboutique.ui.cart.CartItem item) {
                    viewModel.removeItem(item);
                }
            });
        }

        private void observeViewModel() {
            viewModel.getCartListLiveData().observe(getViewLifecycleOwner(), cartItems -> {
                adapter.setCartItems(cartItems);
                btnProceedToCheckout.setEnabled(cartItems != null && !cartItems.isEmpty());
            });

            viewModel.getTotalPriceLiveData().observe(getViewLifecycleOwner(), totalPrice -> {
                tvTotalCost.setText(numberFormat.format(totalPrice));
            });
        }

        private void setupClickEvents() {
            btnProceedToCheckout.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MakeOrderActivity.class);
                startActivity(intent);
            });
        }
    }
}
