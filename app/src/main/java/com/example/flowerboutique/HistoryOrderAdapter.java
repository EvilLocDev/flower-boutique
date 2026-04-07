package com.example.flowerboutique;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.example.flowerboutique.db.entities.OrderModel;
import com.example.flowerboutique.db.entities.ProductItem;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryOrderAdapter extends RecyclerView.Adapter<HistoryOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderModel> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onViewDetail(OrderModel order);
        void onBuyAgain(OrderModel order);
    }

    public HistoryOrderAdapter(Context context, List<OrderModel> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        OrderModel order = orderList.get(position);

        holder.txtOrderId.setText("Mã đơn: " + order.getDocumentId());
        holder.txtCreatedDate.setText(formatTimestamp(order.getCreated_date()));
        holder.txtTotalPrice.setText("Tổng tiền: " + formatCurrency(order.getTotal_price()));

        List<ProductItem> products = order.getProducts();

        if (products != null && !products.isEmpty()) {
            ProductItem firstProduct = products.get(0);

            holder.txtProductName.setText(firstProduct.getName() != null ? firstProduct.getName() : "Không có tên");

            long quantity = firstProduct.getQuantity() != null ? firstProduct.getQuantity() : 0;
            long unitPrice = firstProduct.getUnitPrice() != null ? firstProduct.getUnitPrice() : 0;

            holder.txtQuantity.setText("x" + quantity);
            holder.txtUnitPrice.setText(formatCurrency(unitPrice));

            if (products.size() > 1) {
                holder.txtProductDesc.setText("Và " + (products.size() - 1) + " sản phẩm khác");
            } else {
                holder.txtProductDesc.setText("Trạng thái: " + order.getStatus());
            }
        } else {
            holder.txtProductName.setText("Không có sản phẩm");
            holder.txtProductDesc.setText("Trạng thái: " + order.getStatus());
            holder.txtUnitPrice.setText(formatCurrency(0L));
            holder.txtQuantity.setText("x0");
        }

        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetail(order);
            }
        });

        holder.btnBuyAgain.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBuyAgain(order);
            }
        });

        holder.imgProduct.setImageResource(R.drawable.ic_logo_shop);
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtCreatedDate, txtProductName, txtProductDesc, txtUnitPrice, txtQuantity, txtTotalPrice;
        ImageView imgProduct;
        Button btnViewDetail, btnBuyAgain;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtCreatedDate = itemView.findViewById(R.id.txtCreatedDate);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductDesc = itemView.findViewById(R.id.txtProductDesc);
            txtUnitPrice = itemView.findViewById(R.id.txtUnitPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnBuyAgain = itemView.findViewById(R.id.btnBuyAgain);
        }
    }

    private String formatCurrency(Long amount) {
        if (amount == null) amount = 0L;
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(amount) + " đ";
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }
}