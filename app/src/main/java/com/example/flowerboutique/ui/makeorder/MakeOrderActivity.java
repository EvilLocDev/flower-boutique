package com.example.flowerboutique.ui.makeorder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.databinding.ActivityMakeOrderBinding;
import com.example.flowerboutique.ui.cart.CartItem;
import com.example.flowerboutique.ui.payment.ZaloPayPaymentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MakeOrderActivity extends AppCompatActivity {

    private ActivityMakeOrderBinding binding;
    private long totalAmount = 0L;
    private ArrayList<CartItem> orderItems;
    private final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));

    // =========================================================================
    // =========================== KHU VỰC GIAO DIỆN ===========================
    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMakeOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Nhận dữ liệu từ Intent
        orderItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("list_cart_items");
        totalAmount = getIntent().getLongExtra("total_amount", 0);

        // 2. Setup hiển thị danh sách và tổng tiền
        setupUI();

        // 3. Lắng nghe sự kiện click Đặt hàng
        binding.makeOrderBtn.setOnClickListener(v -> validateAndProcessOrder());

        // Lắng nghe sự kiện nút Back
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupUI() {
        // Hiển thị tổng tiền
        binding.totalPrice.setText(numberFormat.format(totalAmount));

        // Setup RecyclerView
        if (orderItems != null && !orderItems.isEmpty()) {
            MakeOrderAdapter adapter = new MakeOrderAdapter(orderItems);
            // LƯU Ý: Đảm bảo ID này (rvOrderItems) khớp với file XML của bạn
            binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            binding.rvOrderItems.setAdapter(adapter);

            binding.makeOrderBtn.setEnabled(true);
        } else {
            Toast.makeText(this, "Không có sản phẩm nào để thanh toán", Toast.LENGTH_SHORT).show();
            binding.makeOrderBtn.setEnabled(false);
        }
    }

    private void validateAndProcessOrder() {
        // LƯU Ý: Đảm bảo các ID (edtName, edtPhone, edtAddress) khớp với file XML của bạn
        String customerName = binding.edtName.getText().toString().trim();
        String customerPhone = binding.edtPhone.getText().toString().trim();
        String customerAddress = binding.edtAddress.getText().toString().trim();

        // Kiểm tra thông tin
        if (customerName.isEmpty() || customerPhone.isEmpty() || customerAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin khách hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Khóa nút để tránh spam click
        binding.makeOrderBtn.setEnabled(false);
        binding.makeOrderBtn.setText("Đang xử lý...");

        // Chuẩn bị gói dữ liệu
        HashMap<String, Object> orderData = new HashMap<>();
        orderData.put("customerName", customerName);
        orderData.put("customerPhone", customerPhone);
        orderData.put("customerAddress", customerAddress);
        orderData.put("totalAmount", totalAmount);
        orderData.put("status", "pending");
        orderData.put("items", orderItems);
        orderData.put("createdAt", FieldValue.serverTimestamp());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            orderData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        // Gọi hàm xử lý Firebase ở bên dưới
        saveOrderToFirebase(orderData);
    }


    // =========================================================================
    // ======================== KHU VỰC XỬ LÝ NGHIỆP VỤ ========================
    // =========================================================================

    /**
     * Hàm lưu thông tin đơn hàng lên Firebase Firestore
     */
    private void saveOrderToFirebase(HashMap<String, Object> orderData) {
        FirebaseFirestore.getInstance().collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    // Lấy mã ID của đơn hàng vừa tạo thành công
                    String newOrderId = documentReference.getId();

                    // Xóa giỏ hàng local và chuyển sang thanh toán
                    clearCartInRoomDB();
                    navigateToZaloPay(newOrderId);
                })
                .addOnFailureListener(e -> {
                    // Mở lại nút nếu có lỗi
                    binding.makeOrderBtn.setEnabled(true);
                    binding.makeOrderBtn.setText("Đặt hàng");
                    Toast.makeText(MakeOrderActivity.this, "Lỗi khi lưu đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Hàm xóa sạch giỏ hàng trong Room Database (Chạy Thread ẩn để mượt UI)
     */
    private void clearCartInRoomDB() {
        new Thread(() -> {
            if (BoutiqueApplication.getInstance().getRoomDB() != null) {
                BoutiqueApplication.getInstance().getRoomDB().cartDAO().deleteAll();
            }
        }).start();
    }

    /**
     * Hàm chuyển hướng sang màn hình thanh toán ZaloPay
     */
    private void navigateToZaloPay(String orderId) {
        Intent intent = new Intent(MakeOrderActivity.this, ZaloPayPaymentActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("totalAmount", totalAmount);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // Tránh bấm nút Back quay lại trang MakeOrder
        startActivity(intent);
        finish();
    }
}