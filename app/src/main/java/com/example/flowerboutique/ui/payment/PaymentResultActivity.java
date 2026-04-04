package com.example.flowerboutique.ui.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.main_home.HomeActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        TextView tvMessage = findViewById(R.id.message);
        Button btnHome = findViewById(R.id.back_to_home);

        // 1. Xử lý kết quả trả về từ Deep Link (VNPay Redirect)
        Uri data = getIntent().getData();
        if (data != null) {
            String responseCode = data.getQueryParameter("vnp_ResponseCode");
            String orderId = data.getQueryParameter("vnp_TxnRef");

            if ("00".equals(responseCode)) {
                tvMessage.setText("Thanh toán thành công qua VNPay!");
                updateOrderStatus(orderId, "paid");
            } else {
                tvMessage.setText("Giao dịch không thành công hoặc đã bị hủy.");
                updateOrderStatus(orderId, "failed");
            }
        } else {
            // 2. Xử lý dữ liệu truyền qua Intent thông thường (nếu có)
            String status = getIntent().getStringExtra("status");
            String message = getIntent().getStringExtra("message");
            String orderId = getIntent().getStringExtra("orderId");
            tvMessage.setText(message);
            if (status != null && orderId != null) {
                updateOrderStatus(orderId, status);
            }
        }

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentResultActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateOrderStatus(String orderId, String status) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return;
        }

        // In ra Logcat để bạn dễ dò xem orderId truyền về có chính xác không
        Log.d("VNPAY_RESULT", "Đang cập nhật OrderID: " + orderId + " thành: " + status);

        FirebaseFirestore.getInstance().collection("orders").document(orderId)
                .update(
                        "status", status,
                        "payment_date", status.equals("paid") ? FieldValue.serverTimestamp() : null
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("VNPAY_RESULT", "Cập nhật Firestore thành công!");
                })
                .addOnFailureListener(e -> {
                    Log.e("VNPAY_RESULT", "Lỗi Firestore: ", e);
                });
    }
}
