package com.example.flowerboutique.ui.admin.orders;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView orderIdTextView, customerNameTextView, shippingAddressTextView, totalPriceTextView, orderStatusTextView;
    private Button rejectButton, completeButton, backButton;
    private RecyclerView productRecyclerView;
    private AdminOrderDetailProductAdapter productAdapter;
    private List<AdminOrderDetailProduct> productList;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_admin_order_detail);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        orderIdTextView = findViewById(R.id.order_id);
        customerNameTextView = findViewById(R.id.customer_name);
        shippingAddressTextView = findViewById(R.id.shipping_address);
        totalPriceTextView = findViewById(R.id.total_price);
        orderStatusTextView = findViewById(R.id.order_status);
        rejectButton = findViewById(R.id.reject_button);
        completeButton = findViewById(R.id.complete_button);
        backButton = findViewById(R.id.ivBack);

        productRecyclerView = findViewById(R.id.product_list);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new AdminOrderDetailProductAdapter(this, productList);
        productRecyclerView.setAdapter(productAdapter);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetails(orderId);

        rejectButton.setOnClickListener(v -> {
            updateOrderStatus(orderId, "denied", null);
            Toast.makeText(this, "Đã từ chối đơn hàng!", Toast.LENGTH_SHORT).show();
        });

        completeButton.setOnClickListener(v -> {
            updateOrderStatus(orderId, "completed", System.currentTimeMillis());
            Toast.makeText(this, "Đã hoàn thành đơn hàng!", Toast.LENGTH_SHORT).show();
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void loadOrderDetails(String orderId) {
        db.collection("orders").document(orderId).get().addOnSuccessListener(orderSnapshot -> {
            if (orderSnapshot.exists()) {
                orderIdTextView.setText("Mã đơn hàng: " + orderId);

                // FIX: Lấy user_id kiểu String thay vì DocumentReference "user"
                String userId = orderSnapshot.getString("user_id");
                if (userId != null && !userId.equals("guest")) {
                    db.collection("users").document(userId).get().addOnSuccessListener(userSnapshot -> {
                        if (userSnapshot.exists()) {
                            String customerName = userSnapshot.getString("name");
                            customerNameTextView.setText("Tên khách hàng: " + (customerName != null ? customerName : "Chưa cập nhật tên"));
                        } else {
                            customerNameTextView.setText("Tên khách hàng: User không tồn tại");
                        }
                    }).addOnFailureListener(e -> customerNameTextView.setText("Tên khách hàng: Lỗi tải dữ liệu"));
                } else if ("guest".equals(userId)) {
                    customerNameTextView.setText("Tên khách hàng: Khách vãng lai");
                } else {
                    customerNameTextView.setText("Tên khách hàng: N/A");
                }

                Map<String, Object> addressMap = (Map<String, Object>) orderSnapshot.get("address");
                if (addressMap != null) {
                    String address = (String) addressMap.get("address");
                    String city = (String) addressMap.get("city");
                    String district = (String) addressMap.get("district");
                    String ward = (String) addressMap.get("ward");
                    String fullAddress = address + ", " + district + ", " + ward + ", " + city;
                    shippingAddressTextView.setText("Địa chỉ giao hàng: " + fullAddress);
                }

                String status = orderSnapshot.getString("status");
                orderStatusTextView.setText("Trạng thái: " + translateStatus(status));

                if ("pending".equals(status) || "paid".equals(status)) {
                    updateButtonState(rejectButton, true);
                    updateButtonState(completeButton, true);
                } else if ("paying".equals(status)) {
                    updateButtonState(rejectButton, true);
                    updateButtonState(completeButton, false);
                } else {
                    updateButtonState(rejectButton, false);
                    updateButtonState(completeButton, false);
                }

                List<Map<String, Object>> products = (List<Map<String, Object>>) orderSnapshot.get("products");
                if (products != null) {
                    productList.clear();
                    AtomicReference<Double> totalPrice = new AtomicReference<>(0.0);
                    for (Map<String, Object> productMap : products) {
                        DocumentReference productRef = (DocumentReference) productMap.get("product");
                        if (productRef != null) {
                            productRef.get().addOnSuccessListener(productSnapshot -> {
                                if (productSnapshot.exists()) {
                                    String productName = productSnapshot.getString("name");
                                    String productDescription = productSnapshot.getString("description");
                                    Double pPrice = productSnapshot.getDouble("price");
                                    double productPrice = (pPrice != null) ? pPrice : 0.0;

                                    List<String> images = (List<String>) productSnapshot.get("image");
                                    String productImage = images != null && !images.isEmpty() ? images.get(0) : null;
                                    
                                    Long qtyLong = (Long) productMap.get("quantity");
                                    int quantity = (qtyLong != null) ? qtyLong.intValue() : 0;
                                    
                                    totalPrice.updateAndGet(v -> v + productPrice * quantity);

                                    productList.add(new AdminOrderDetailProduct(productName, productDescription, String.valueOf(productPrice), productImage, quantity));
                                    productAdapter.notifyDataSetChanged();
                                    totalPriceTextView.setText("Tổng tiền: " + String.format("%,.0f", totalPrice.get()) + " đ");
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private String translateStatus(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "pending": return "Đang đợi xử lý";
            case "paying": return "Đang đợi thanh toán";
            case "paid": return "Đã thanh toán (Chờ giao)";
            case "completed": return "Đã giao thành công";
            case "denied": return "Đã từ chối";
            case "failed": return "Thanh toán thất bại";
            default: return "Trạng thái: " + status;
        }
    }

    private void updateOrderStatus(String orderId, String status, Long completedDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        if (completedDate != null) {
            updates.put("completed_date", new Timestamp(new java.util.Date(completedDate)));
        }

        db.collection("orders").document(orderId).update(updates).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent();
            intent.putExtra("id", orderId);
            setResult(AdminOrderDetailActivity.RESULT_OK, intent);
            loadOrderDetails(orderId);
        }).addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show());
    }

    private void updateButtonState(Button button, boolean isEnabled) {
        button.setEnabled(isEnabled);
        if (isEnabled) {
            int colorRes = (button.getId() == R.id.reject_button) ? R.color.red : R.color.green;
            button.setBackgroundTintList(getResources().getColorStateList(colorRes, null));
            button.setTextColor(getResources().getColor(R.color.white, null));
        } else {
            button.setBackgroundTintList(getResources().getColorStateList(R.color.gray, null));
            button.setTextColor(getResources().getColor(R.color.grey_light, null));
        }
    }
}
