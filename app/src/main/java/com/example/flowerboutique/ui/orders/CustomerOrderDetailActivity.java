package com.example.flowerboutique.ui.orders;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.payment.VNPayPaymentActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CustomerOrderDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView orderIdTextView, customerNameTextView, shippingAddressTextView, totalPriceTextView, orderStatusTextView;
    private Button btn_checkout, backBtn;
    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail_customer);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String id = intent.getStringExtra("order_id");
        if (id == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Ánh xạ View từ file activity_product_detail_customer.xml
        orderIdTextView = findViewById(R.id.txt_order_id);
        shippingAddressTextView = findViewById(R.id.txt_address);
        totalPriceTextView = findViewById(R.id.txt_total_price);
        orderStatusTextView = findViewById(R.id.txt_status);
        btn_checkout = findViewById(R.id.btn_checkout);
        backBtn = findViewById(R.id.ivBack);
        productRecyclerView = findViewById(R.id.product_list);

        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        productRecyclerView.setAdapter(productAdapter);

        backBtn.setOnClickListener(v -> finish());

        loadOrderDetails(id);
    }

    private void loadOrderDetails(String orderId) {
        db.collection("orders").document(orderId).get().addOnSuccessListener(orderSnapshot -> {
            if (orderSnapshot.exists()) {

                orderIdTextView.setText("Mã đơn hàng: " + orderId);

                // Xử lý nút thanh toán VNPay
                Long totalPriceFirestore = orderSnapshot.getLong("total_price");
                long finalTotalPrice = (totalPriceFirestore != null) ? totalPriceFirestore : 0L;

                btn_checkout.setOnClickListener(v -> {
                    Intent intent = new Intent(CustomerOrderDetailActivity.this, VNPayPaymentActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("totalPrice", finalTotalPrice); // Gửi số tiền sang màn hình VNPay
                    startActivity(intent);
                });

                // Lấy thông tin user
                DocumentReference userRef = (DocumentReference) orderSnapshot.get("user");
                if (userRef != null) {
                    userRef.get().addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            customerNameTextView.setText("Tên khách hàng: " + userSnapshot.getString("name"));
                        } else {
                            customerNameTextView.setText("Tên khách hàng: N/A");
                        }
                    });
                }

                // Lấy địa chỉ giao hàng
                Map<String, Object> addressMap = (Map<String, Object>) orderSnapshot.get("address");
                if (addressMap != null) {
                    String address = (String) addressMap.get("address");
                    String city = (String) addressMap.get("city");
                    String district = (String) addressMap.get("district");
                    String ward = (String) addressMap.get("ward");
                    shippingAddressTextView.setText(String.format("Địa chỉ giao hàng: %s, %s, %s, %s", address, district, ward, city));
                }

                // Cập nhật trạng thái
                String status = orderSnapshot.getString("status");
                orderStatusTextView.setText("Trạng thái: " + translateStatus(status));

                // Bật/tắt nút thanh toán
                updateButtonState(btn_checkout, "paying".equals(status));

                // Load danh sách sản phẩm
                List<Map<String, Object>> products = (List<Map<String, Object>>) orderSnapshot.get("products");
                if (products != null) {
                    AtomicReference<Double> calculatedTotal = new AtomicReference<>(0.0);
                    for (Map<String, Object> productMap : products) {
                        DocumentReference productRef = (DocumentReference) productMap.get("product");
                        if(productRef != null) {
                            productRef.get().addOnSuccessListener(productSnapshot -> {
                                if (productSnapshot.exists()) {
                                    String productName = productSnapshot.getString("name");
                                    double productPrice = productSnapshot.getDouble("price") != null ? productSnapshot.getDouble("price") : 0.0;
                                    List<String> images = (List<String>) productSnapshot.get("image");
                                    String productImage = (images != null && !images.isEmpty()) ? images.get(0) : null;
                                    int quantity = ((Long) productMap.get("quantity")).intValue();

                                    calculatedTotal.updateAndGet(v -> v + productPrice * quantity);

                                    productList.add(new Product(quantity, productImage, String.valueOf(productPrice), productName));
                                    productAdapter.notifyDataSetChanged();

                                    // Hiển thị tổng tiền
                                    totalPriceTextView.setText("Tổng tiền: \n" + String.format("%,.0f", finalTotalPrice > 0 ? finalTotalPrice : calculatedTotal.get()) + " đ");
                                }
                            });
                        }
                    }
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Không thể tải thông tin đơn hàng!", Toast.LENGTH_SHORT).show());
    }

    private String translateStatus(String status) {
        if (status == null) return "Chờ xử lý"; // Trạng thái mặc định

        switch (status.toLowerCase()) {
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
                return status; // Trả về text gốc nếu không khớp
        }
    }

    private void updateButtonState(Button button, boolean isEnabled) {
        button.setEnabled(isEnabled);
        button.setBackgroundColor(Color.parseColor(isEnabled ? "#0050e7" : "#BDBDBD")); // Xám nếu không khả dụng
        button.setText(isEnabled ? "Thanh toán ngay" : "Không thể thanh toán");
    }
}