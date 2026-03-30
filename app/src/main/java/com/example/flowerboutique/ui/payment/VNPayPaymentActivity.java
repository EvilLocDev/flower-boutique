package com.example.flowerboutique.ui.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerboutique.R;
import com.example.flowerboutique.utils.vnpay.VNPayUtil; // Bạn cần tạo class này
import com.example.flowerboutique.utils.vnpay.ResponseVNPayBody; // Class hứng Model từ API

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VNPayPaymentActivity extends AppCompatActivity {

    private String orderId;
    private long totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_payment); // Có thể giữ nguyên layout cũ hoặc đổi tên

        orderId = getIntent().getStringExtra("orderId");
        totalAmount = getIntent().getLongExtra("totalPrice", 0L);

        Log.d("VNPAY_DEBUG", "Gửi lên Server: " + orderId + " - " + totalAmount);

        if (orderId == null || totalAmount == 0L) {
            Toast.makeText(this, "Dữ liệu đơn hàng không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnPayment = findViewById(R.id.payment_btn);
        btnPayment.setText("Thanh toán qua VNPay");
        btnPayment.setOnClickListener(v -> requestVNPay());
    }

    private void requestVNPay() {
        VNPayUtil.createPayment(orderId, totalAmount)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        // Trong file VNPayPaymentActivity.java, sửa đoạn xử lý response:
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String paymentUrl = response.body().string();
                                // Xử lý chuỗi link cực kỳ cẩn thận
                                paymentUrl = paymentUrl.trim().replace("\"", "");

                                if (paymentUrl.startsWith("http")) {
                                    Log.d("VNPAY_DEBUG", "Đang mở URL sạch: " + paymentUrl);

                                    // Ép trình duyệt mở link trong một Task mới để tránh bị ảnh hưởng bởi SSL của App
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                    // Thông báo cho người dùng
                                    runOnUiThread(() -> Toast.makeText(VNPayPaymentActivity.this, "Đang chuyển đến VNPay...", Toast.LENGTH_SHORT).show());
                                }
                            } catch (Exception e) {
                                Log.e("VNPAY_DEBUG", "Lỗi khi mở trình duyệt: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        handleResult("failed", "Lỗi kết nối: " + t.getMessage());
                    }
                });

    }
    private void openVNPayWeb(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
        Toast.makeText(this, "Đang mở VNPay...", Toast.LENGTH_SHORT).show();
    }

    private void handleResult(String status, String message) {
        Intent intent = new Intent(this, PaymentResultActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("status", status);
        intent.putExtra("message", message);
        startActivity(intent);
        finish();
    }
}