package com.example.flowerboutique.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.flowerboutique.R; // Nhớ đổi đúng package của bạn

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));
    private CartItemActionListener actionListener;

    // Interface để giao tiếp với Activity
    public interface CartItemActionListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onRemove(CartItem item);
    }

    public void setActionListener(CartItemActionListener listener) {
        this.actionListener = listener;
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(numberFormat.format(item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        holder.btnIncrease.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onIncrease(item);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDecrease(item);
        });

        holder.btnTrash.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onRemove(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems == null ? 0 : cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        Button btnIncrease, btnDecrease, btnTrash;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.product_name);
            tvPrice = itemView.findViewById(R.id.product_price);
            tvQuantity = itemView.findViewById(R.id.product_quantity);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQuantity);
            btnTrash = itemView.findViewById(R.id.btn_trash);
        }
    }
}