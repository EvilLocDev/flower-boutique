package com.example.flowerboutique.ui.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
        String firebaseStatus = "pending";
        if (status.equals("success")) firebaseStatus = "paid";
        else if (status.equals("failed")) firebaseStatus = "failed";

        FirebaseFirestore.getInstance().collection("orders").document(orderId)
                .update(
                        "status", firebaseStatus,
                        "payment_date", firebaseStatus.equals("paid") ? FieldValue.serverTimestamp() : null
                );
    }
}