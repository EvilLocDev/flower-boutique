package com.example.flowerboutique.ui.orders;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder> {

    private final List<CustomerOrderOverview> customerOrderOverviewList;

    public CustomerOrderAdapter(List<CustomerOrderOverview> list) {
        this.customerOrderOverviewList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerOrderOverview order = customerOrderOverviewList.get(position);
        holder.nameTx.setText(order.name);
        holder.priceTx.setText(order.price);
        holder.dateTx.setText(order.date);
        holder.orderId.setText(String.format("Mã: %s", order.id));
        holder.stateTx.setText(getOrderStateText(order.state));
        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent(holder.view.getContext(), CustomerOrderDetailActivity.class);
            intent.putExtra("order_id", order.id);
            holder.view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return customerOrderOverviewList.size();
    }

    // Hàm tiện ích để ánh xạ trạng thái đơn hàng
    private String getOrderStateText(String state) {
        if (state == null) return "Chờ xử lý"; // Tránh lỗi NullPointerException

        switch (state.toLowerCase()) {
            case "pending":
            case "paying":
                return "Chờ xử lý";

            case "delivering":
            case "shipping":
                return "Đang giao";

            case "completed":
            case "paid":
            case "success":
                return "Hoàn thành";

            case "cancelled":
            case "failed":
            case "denied":
                return "Đã hủy";

            default:
                return state; // Trả về nguyên gốc nếu có trạng thái lạ
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTx, priceTx, dateTx, stateTx, orderId;
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTx = itemView.findViewById(R.id.name_tx);
            priceTx = itemView.findViewById(R.id.price_tx);
            dateTx = itemView.findViewById(R.id.date_tx);
            stateTx = itemView.findViewById(R.id.state_tx);
            orderId = itemView.findViewById(R.id.order_id);
            view = itemView;
        }
    }

    public static class CustomerOrderOverview {
        String id;
        String name;
        String date;
        String state;
        String price;

        public CustomerOrderOverview(String id, String name, String date, String state, String price) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.state = state;
            this.price = price;
        }
    }
}