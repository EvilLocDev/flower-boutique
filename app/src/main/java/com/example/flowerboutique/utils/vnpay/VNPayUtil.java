package com.example.flowerboutique.utils.vnpay;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class VNPayUtil {

    // 1. THÊM dấu / ở cuối link
    private static final String BASE_URL = "https://createvnpayurl-hizrvhhlfa-uc.a.run.app/";

    interface VNPayService {
        @FormUrlEncoded
        // 2. SỬA THÀNH "/" vì link trên đã trỏ trực tiếp vào hàm rồi
        @POST("/")
        Call<ResponseBody> createVNPayOrder(
                @Field("orderId") String orderId,
                @Field("amount") long amount
        );
    }

    public static Call<ResponseBody> createPayment(String orderId, long amount) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VNPayService service = retrofit.create(VNPayService.class);
        return service.createVNPayOrder(orderId, amount);
    }
}