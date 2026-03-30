package com.example.flowerboutique.ui.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerboutique.R;
import com.example.flowerboutique.utils.vnpay.VNPayUtil; // Bạn cần tạo class này
import com.example.flowerboutique.utils.vnpay.ResponseVNPayBody; // Class hứng Model từ API

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
        // VNPay yêu cầu tạo URL từ Backend để bảo mật Checksum/Hash
        VNPayUtil.createPayment(orderId, totalAmount)
                .enqueue(new Callback<ResponseVNPayBody>() {
                    @Override
                    public void onResponse(Call<ResponseVNPayBody> call, Response<ResponseVNPayBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String paymentUrl = response.body().getPaymentUrl();
                            if (paymentUrl != null) {
                                openVNPayWeb(paymentUrl);
                            }
                        } else {
                            handleResult("failed", "Lỗi tạo link thanh toán VNPay");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseVNPayBody> call, Throwable t) {
                        handleResult("failed", "Lỗi mạng: " + t.getMessage());
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