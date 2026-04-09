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
import com.example.flowerboutique.ui.orders.CustomerOrdersActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        TextView tvMessage = findViewById(R.id.message);
        Button btnHome = findViewById(R.id.back_to_home);
        Button btnOrderDetail = findViewById(R.id.to_order_detail);

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

        // 2. Xử lý sự kiện "Về trang chủ"
        btnHome.setOnClickListener(v -> {
            // Thay HomeActivity.class bằng tên Activity màn hình chính của bạn (ví dụ: MainActivity.class)
            Intent intent = new Intent(PaymentResultActivity.this, HomeActivity.class);
            // Xóa tất cả các màn hình cũ đang xếp chồng lên nhau
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentResultActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // 3. Xử lý sự kiện "Xem đơn hàng"
        btnOrderDetail.setOnClickListener(v -> {
            // LƯU Ý: "item_customer_order" chỉ là tên của một giao diện (Layout) chứ không phải Activity.
            // Do đó bạn cần gọi đến Activity chứa cái danh sách đơn hàng đó (Ví dụ: OrderHistoryActivity, CustomerOrderActivity...)

            Intent intent = new Intent(PaymentResultActivity.this, com.example.flowerboutique.ui.orders.CustomerOrdersActivity.class); // <- THAY TÊN ACTIVITY CỦA BẠN VÀO ĐÂY

            // Bạn có thể truyền luôn mã đơn hàng vừa thanh toán sang màn hình đó để tra cứu nếu cần
            // intent.putExtra("orderId", orderId);

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
