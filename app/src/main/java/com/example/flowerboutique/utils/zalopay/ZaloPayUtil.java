package com.example.flowerboutique.utils.zalopay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Locale;

import com.example.flowerboutique.BuildConfig;
import com.example.flowerboutique.utils.MACGenerator;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ZaloPayUtil {

    private static final Gson gson = new GsonBuilder().create();

    // Dùng endpoint chính thức cho Sandbox V2
    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://sb-openapi.zalopay.vn/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    private static final ZaloPayApi api = retrofit.create(ZaloPayApi.class);

    public static Call<ResponseCreateZalopayOrderBody> createPayment(String orderId, String uid, long totalPrice, String transId) {
        RequestCreateZalopayOrderBody data = new RequestCreateZalopayOrderBody();

        data.app_id = Integer.parseInt(BuildConfig.ZALO_PAY_APP_ID);
        data.app_user = uid;
        data.app_trans_id = transId;
        data.app_time = System.currentTimeMillis();
        data.amount = totalPrice;
        data.description = "Thanh toan don hang #" + orderId;
        data.item = "[]";
        data.embed_data = "{}";
        data.bank_code = "";

        try {
            // Đối với V2, chuỗi MacData vẫn là: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
            // Nhưng cần đảm bảo không có bất kỳ khoảng trắng nào
            String macData = data.app_id + "|" + 
                            data.app_trans_id + "|" + 
                            data.app_user + "|" + 
                            data.amount + "|" + 
                            data.app_time + "|" + 
                            data.embed_data + "|" + 
                            data.item;

            android.util.Log.d("ZALOPAY_LOG", "Chuỗi MacData (V2): " + macData);

            data.mac = MACGenerator.computeMac(macData, BuildConfig.ZALO_PAY_KEY1.trim());
            
            android.util.Log.d("ZALOPAY_LOG", "MAC tạo ra: " + data.mac);

        } catch (Exception e) {
            android.util.Log.e("ZALOPAY_LOG", "Lỗi tạo MAC: " + e.getMessage());
            data.mac = "";
        }

        return api.createOrder(data);
    }

    public static Call<Object> queryTransStatus(String appTransId) {
        RequestQueryTransStatus data = new RequestQueryTransStatus(
                Integer.parseInt(BuildConfig.ZALO_PAY_APP_ID),
                appTransId,
                ""
        );

        try {
            String macData = data.app_id + "|" + data.app_trans_id + "|" + BuildConfig.ZALO_PAY_KEY1.trim();
            data.mac = MACGenerator.computeMac(macData, BuildConfig.ZALO_PAY_KEY1.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return api.queryStatus(data);
    }
}
