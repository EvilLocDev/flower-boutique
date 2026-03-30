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
                updateOrderStatus(orderId, "success");
            } else {
                tvMessage.setText("Giao dịch không thành công hoặc đã bị hủy.");
                updateOrderStatus(orderId, "failed");
            }
        } else {
            // 2. Xử lý dữ liệu truyền qua Intent thông thường (nếu có)
            String message = getIntent().getStringExtra("message");
            tvMessage.setText(message);
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
            Toast.makeText(this, "Không tìm thấy mã đơn hàng từ VNPay", Toast.LENGTH_SHORT).show();
            return;
        }

        String firebaseStatus = "pending";
        if (status.equals("success")) firebaseStatus = "paid";
        else if (status.equals("failed")) firebaseStatus = "failed";

        // In ra Logcat để bạn dễ dò xem orderId truyền về có chính xác không
        Log.d("VNPAY_RESULT", "Đang cập nhật OrderID: " + orderId + " thành: " + firebaseStatus);

        FirebaseFirestore.getInstance().collection("orders").document(orderId)
                .update(
                        "status", firebaseStatus,
                        "payment_date", firebaseStatus.equals("paid") ? FieldValue.serverTimestamp() : null
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("VNPAY_RESULT", "Cập nhật Firestore thành công!");
                    Toast.makeText(PaymentResultActivity.this, "Đã ghi nhận thanh toán thành công vào DB!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("VNPAY_RESULT", "Lỗi Firestore: ", e);
                    Toast.makeText(PaymentResultActivity.this, "Lỗi cập nhật CSDL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}