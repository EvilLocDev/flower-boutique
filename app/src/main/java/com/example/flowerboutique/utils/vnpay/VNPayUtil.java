package com.example.flowerboutique.utils.vnpay;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class VNPayUtil {

    // Thay đổi URL này thành URL API thực tế của bạn
    private static final String BASE_URL = "https://your-backend-api.com/";

    interface VNPayService {
        @FormUrlEncoded
        @POST("api/payment/create-vnpay") // Endpoint tạo link VNPay trên Server
        Call<ResponseVNPayBody> createVNPayOrder(
                @Field("orderId") String orderId,
                @Field("amount") long amount
        );
    }

    public static Call<ResponseVNPayBody> createPayment(String orderId, long amount) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VNPayService service = retrofit.create(VNPayService.class);
        return service.createVNPayOrder(orderId, amount);
    }
}