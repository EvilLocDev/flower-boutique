package com.example.flowerboutique.utils.vnpay;

import com.google.gson.annotations.SerializedName;

public class ResponseVNPayBody {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("paymentUrl") // Link thanh toán VNPay do Server tạo ra
    private String paymentUrl;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }
}