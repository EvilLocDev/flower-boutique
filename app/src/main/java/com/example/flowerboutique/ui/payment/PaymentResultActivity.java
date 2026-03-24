package com.example.flowerboutique.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.flowerboutique.R; // Thay bằng R của bạn nếu dùng ViewBinding thì càng tốt
import com.example.flowerboutique.ui.main_home.HomeActivity; // Thay bằng đường dẫn tới Activity Trang Chủ của bạn

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIXME: Thay R.layout.activity_payment_result bằng ViewBinding của bạn nếu có
        setContentView(R.layout.activity_payment_result);

        TextView tvMessage = findViewById(R.id.message);
        Button btnHome = findViewById(R.id.back_to_home);

        // Lấy dữ liệu từ ZaloPayPaymentActivity truyền qua
        String status = getIntent().getStringExtra("status");
        String message = getIntent().getStringExtra("message");
        String orderId = getIntent().getStringExtra("orderId");

        // Hiển thị thông báo
        tvMessage.setText(message);

        // Cập nhật trạng thái đơn hàng lên Firebase
        if (orderId != null && status != null) {
            updateOrderStatus(orderId, status);
        }

        // Bấm nút quay về trang chủ
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentResultActivity.this, HomeActivity.class);
            // Cờ này giúp xóa hết các màn hình trung gian (Giỏ hàng, Thanh toán...)
            // để người dùng không bấm Back lại màn hình thanh toán được nữa
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateOrderStatus(String orderId, String status) {
        // Map status của ZaloPay thành status của hệ thống bạn
        String firebaseStatus = "pending";
        if (status.equals("success")) firebaseStatus = "paid";
        else if (status.equals("canceled")) firebaseStatus = "canceled";
        else if (status.equals("failed")) firebaseStatus = "failed";

        FirebaseFirestore.getInstance().collection("orders").document(orderId)
                .update(
                        "status", firebaseStatus,
                        "payment_date", firebaseStatus.equals("paid") ? FieldValue.serverTimestamp() : null
                )
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật thành công
                })
                .addOnFailureListener(e -> {
                    // Báo lỗi log nếu cần
                });
    }
}