package com.example.flowerboutique.ui.payment;

import android.content.Intent;
import android.net.Uri;
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
        setContentView(R.layout.activity_zalo_pay);

        orderId = getIntent().getStringExtra("orderId");
        totalAmount = getIntent().getLongExtra("totalPrice", 0L);

        android.util.Log.d("ZALOPAY_LOG", "SỐ TIỀN TRUYỀN VÀO LÀ: " + totalAmount);

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

        Button btnPayment = findViewById(R.id.payment_btn);
        btnPayment.setOnClickListener(v -> {
            requestZaloPay();
        });
    }

    private void requestZaloPay() {
        String transId = new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + "_" + orderId;

        ZaloPayUtil.createPayment(orderId, uid, totalAmount, transId)
                .enqueue(new Callback<ResponseCreateZalopayOrderBody>() {
                    @Override
                    public void onResponse(Call<ResponseCreateZalopayOrderBody> call, Response<ResponseCreateZalopayOrderBody> response) {
                        ResponseCreateZalopayOrderBody body = response.body();
                        if (body == null) return;

                        int returnCode = body.getReturnCode();
                        String orderUrl = body.getOrderUrl();
                        String subMessage = body.getSubReturnMessage();

                        android.util.Log.d("ZALOPAY_LOG", "Mã phản hồi: " + returnCode);
                        android.util.Log.d("ZALOPAY_LOG", "Order URL: " + orderUrl);

                        if (returnCode == 1 && orderUrl != null) {
                            // CÁCH CHỐNG CHÁY: Mở bằng trình duyệt
                            openZaloPayWeb(orderUrl);
                        } else {
                            android.util.Log.e("ZALOPAY_LOG", "ZaloPay từ chối tạo đơn. Lý do: " + subMessage);
                            handleResult("failed", "Lỗi tạo đơn: " + subMessage);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseCreateZalopayOrderBody> call, Throwable t) {
                        handleResult("failed", "Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void openZaloPayWeb(String url) {
        // Mở trình duyệt web để thanh toán (Thay vì mở app qua SDK)
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
        
        // Lưu ý: Khi mở trình duyệt, App của bạn sẽ không nhận được kết quả ngay lập tức
        // Bạn có thể cần một nút "Tôi đã thanh toán xong" hoặc tự động Query lại trạng thái sau khi người dùng quay lại app.
        Toast.makeText(this, "Đang mở trình duyệt để thanh toán...", Toast.LENGTH_LONG).show();
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
