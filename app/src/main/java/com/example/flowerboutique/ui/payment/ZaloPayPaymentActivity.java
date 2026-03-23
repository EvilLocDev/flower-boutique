package com.example.flowerboutique.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerboutique.R;
import com.example.flowerboutique.utils.zalopay.ResponseCreateZalopayOrderBody;
import com.example.flowerboutique.utils.zalopay.ZaloPayUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ZaloPayPaymentActivity extends AppCompatActivity {

    private String orderId;
    private long totalAmount;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Gắn file giao diện xml này vào màn hình
        setContentView(R.layout.activity_zalo_pay);

        // Nhận dữ liệu từ MakeOrderActivity truyền sang
        orderId = getIntent().getStringExtra("orderId");
        totalAmount = getIntent().getLongExtra("totalAmount", 0L);

        if (orderId == null || totalAmount == 0L) {
            Toast.makeText(this, "Dữ liệu đơn hàng không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            uid = "guest_user";
        }

        // 2. Ánh xạ nút bấm và bắt sự kiện Click
        Button btnPayment = findViewById(R.id.payment_btn);
        btnPayment.setOnClickListener(v -> {
            // Khi người dùng bấm nút mới bắt đầu gọi ZaloPay
            requestZaloPay();
        });
    }

    private void requestZaloPay() {
        // Tạo mã giao dịch (transId) theo chuẩn yyMMdd_HHmmss_orderId
        String transId = new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + "_" + orderId;

        // Sử dụng ZaloPayUtil cực kỳ gọn gàng của bạn
        ZaloPayUtil.createPayment(orderId, uid, totalAmount, transId)
                .enqueue(new Callback<ResponseCreateZalopayOrderBody>() {
                    @Override
                    public void onResponse(Call<ResponseCreateZalopayOrderBody> call, Response<ResponseCreateZalopayOrderBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ResponseCreateZalopayOrderBody body = response.body();

                            // return_code = 1 là ZaloPay báo tạo đơn thành công
                            if (body.return_code == 1) {
                                String token = body.zpTransToken;
                                openZaloPayApp(token);
                            } else {
                                handleResult("failed", "Lỗi từ ZaloPay: " + body.return_message);
                            }
                        } else {
                            handleResult("failed", "Lỗi kết nối đến máy chủ ZaloPay");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseCreateZalopayOrderBody> call, Throwable t) {
                        handleResult("failed", "Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void openZaloPayApp(String token) {
        // Gọi SDK mở App ZaloPay
        ZaloPaySDK.getInstance().payOrder(this, token, "flowerstore://app", new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(String transactionId, String transToken, String appTransId) {
                handleResult("success", "Thanh toán thành công!");
            }

            @Override
            public void onPaymentCanceled(String zpTransToken, String appTransId) {
                handleResult("canceled", "Bạn đã hủy thanh toán.");
            }

            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransId) {
                handleResult("failed", "Thanh toán thất bại: " + zaloPayError.toString());
            }
        });
    }

    private void handleResult(String status, String message) {
        // Chuyển kết quả sang màn hình PaymentResultActivity
        Intent intent = new Intent(this, PaymentResultActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("status", status);
        intent.putExtra("message", message);
        startActivity(intent);
        finish();
    }

    // HÀM BẮT BUỘC: Để ZaloPay trả kết quả về app
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}