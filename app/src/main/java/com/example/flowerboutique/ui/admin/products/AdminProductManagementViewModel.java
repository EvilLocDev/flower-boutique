package com.example.flowerboutique.ui.admin.products;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductManagementViewModel extends ViewModel {
    private final List<Product> products = new ArrayList<>();
    private final ProductAdapter productAdapter = new ProductAdapter(products);
    private final CollectionReference productCollection = (new AppFirebase()).getProductsCollection();

    @SuppressLint("NotifyDataSetChanged")
    public AdminProductManagementViewModel() {
        productAdapter.setOnDelete(product -> {
            productCollection.document(product.id).delete();
            products.remove(product);
            productAdapter.notifyDataSetChanged();
        });
        loadData();
    }

    public List<Product> getProducts() {
        return products;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadData() {
//        productCollection.orderBy("created_date", Query.Direction.DESCENDING).orderBy(FieldPath.documentId(), Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
        productCollection.orderBy("created_date", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                snapshot.getDocuments().forEach(productDoc -> {
                    List<String> imageList = ((List<String>) productDoc.get("image"));
                    String image = imageList != null && imageList.get(0) != null ? imageList.get(0) : null;
                    products.add(new Product(productDoc.getId(), productDoc.getString("name"), productDoc.getLong("price"), productDoc.getBoolean("status"), image));
                });
                productAdapter.notifyDataSetChanged();
            } else {
                android.util.Log.e("FirebaseError", "Lỗi tải sản phẩm: ", task.getException());
            }
        });
    }

    public ProductAdapter getProductAdapter() {
        return productAdapter;
    }

    static public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private OnClickEvent onDelete;
        private OnClickEvent onClick;
        private List<Product> products;
        private final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));

        public ProductAdapter(List<Product> products) {
            this.products = products;
        }

        public void setOnDelete(OnClickEvent onDelete) {
            this.onDelete = onDelete;
        }

        public static interface OnClickEvent {
            void onClickEvent(Product product);
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public void setOnClick(OnClickEvent onClick) {
            this.onClick = onClick;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.titleTv.setText(product.getName());
            holder.priceTv.setText(numberFormat.format(product.getPrice()));
            holder.statusTv.setText(product.status ? "Đang hoạt động" : "Không hoạt động");
            Picasso.get().load(product.image).into(holder.productImg);
            if (onDelete != null)
                holder.deleteBtn.setOnClickListener(v -> onDelete.onClickEvent(product));
            if (onClick != null)
                holder.view.setOnClickListener(v -> onClick.onClickEvent(product));
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        static public class ProductViewHolder extends RecyclerView.ViewHolder {
            private final View view;
            private final TextView titleTv, priceTv, statusTv;
            private final ImageView productImg, deleteBtn;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                deleteBtn = itemView.findViewById(R.id.delete_btn);
                productImg = itemView.findViewById(R.id.product_img);
                titleTv = itemView.findViewById(R.id.product_name_tv);
                priceTv = itemView.findViewById(R.id.price_tv);
                statusTv = itemView.findViewById(R.id.status_tv);
                view = itemView;
            }
        }
    }

    static public class Product {
        String id;
        String name;
        long price;
        boolean status;
        String image;

        public Product(String id, String name, long price, boolean status, String image) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.status = status;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getPrice() {
            return price;
        }

        public void setPrice(long price) {
            this.price = price;
        }

        public boolean getStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
