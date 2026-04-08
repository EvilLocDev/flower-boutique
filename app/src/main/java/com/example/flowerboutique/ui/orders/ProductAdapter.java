package com.example.flowerboutique.ui.orders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu ý: Đảm bảo bạn có file layout product_detail_customer_tyni.xml
        View itemView = LayoutInflater.from(context).inflate(R.layout.product_detail_customer_tyni, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());

        try {
            double productPrice = Double.parseDouble(product.getPrice());
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            String formattedPrice = decimalFormat.format(productPrice) + " đ";
            holder.productPrice.setText(formattedPrice);
        } catch (NumberFormatException e) {
            holder.productPrice.setText(product.getPrice() + " đ");
        }

        holder.productQuantity.setText("Số lượng: " + product.getQuantity());

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground) // Đổi thành placeholder của bạn nếu có
                    .into(holder.productImage);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImage;
        public TextView productName, productPrice, productQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_img);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productQuantity = itemView.findViewById(R.id.product_quantity);
        }
    }
}