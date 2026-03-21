package com.example.flowerboutique.ui.makeorder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.cart.CartItem;

public class MakeOrderAdapter extends RecyclerView.Adapter<MakeOrderAdapter.OrderViewHolder> {

    private List<CartItem> items;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));

    public MakeOrderAdapter(List<CartItem> items) {
        this.items = items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_make_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(currencyFormat.format(item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        // (Tùy chọn) Dùng Picasso load ảnh vào holder.imgProduct nếu cần
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.product_name);
            tvPrice = itemView.findViewById(R.id.price);
            tvQuantity = itemView.findViewById(R.id.quantity);
        }
    }
}