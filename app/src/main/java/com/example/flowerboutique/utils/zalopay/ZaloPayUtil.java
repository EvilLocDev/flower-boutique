package com.example.flowerboutique.utils.zalopay;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.Locale;

// TODO: Kiểm tra lại 2 dòng import này xem đã trỏ đúng vào thư mục chứa file của bạn chưa nhé
import com.example.flowerboutique.BuildConfig;
import com.example.flowerboutique.utils.MACGenerator;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ZaloPayUtil {

    // Cấu hình Gson tự động chuyển đổi camelCase sang snake_case (JSON của ZaloPay)
    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    // Khởi tạo Retrofit (Singleton Pattern)
    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://sb-openapi.zalopay.vn/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    // Tạo API Client
    private static final ZaloPayApi api = retrofit.create(ZaloPayApi.class);

    /**
     * Hàm tạo yêu cầu thanh toán (Lấy Token để mở app ZaloPay)
     */
    public static Call<ResponseCreateZalopayOrderBody> createPayment(String orderId, String uid, long totalPrice, String transId) {
        RequestCreateZalopayOrderBody data = new RequestCreateZalopayOrderBody();

        // Lấy thông tin từ BuildConfig
        data.app_id = Integer.parseInt(BuildConfig.ZALO_PAY_APP_ID);
        data.app_user = uid;
        data.app_trans_id = transId;
        data.app_time = (new Date()).getTime();
        data.amount = totalPrice;
        data.description = String.format("Thanh toán đơn hàng %s", orderId);
        data.item = "[]";
        data.embed_data = "{}";

        try {
            // Chuỗi dữ liệu chuẩn để tạo MAC theo yêu cầu của ZaloPay
            String macData = String.format(new Locale("vi", "vn"), "%d|%s|%s|%d|%d|%s|%s",
                    data.app_id,
                    data.app_trans_id,
                    data.app_user,
                    data.amount,
                    data.app_time,
                    data.embed_data,
                    data.item);

            // Băm MAC bằng KEY 1
            data.mac = MACGenerator.computeMac(macData, BuildConfig.ZALO_PAY_KEY1);

        } catch (Exception e) {
            e.printStackTrace();
            data.mac = "";
        }

        return api.createOrder(data);
    }

    /**
     * Hàm kiểm tra trạng thái đơn hàng (Dành cho việc query xem khách đã trả tiền chưa)
     */
    public static Call<Object> queryTransStatus(String appTransId) {
        RequestQueryTransStatus data = new RequestQueryTransStatus(
                Integer.parseInt(BuildConfig.ZALO_PAY_APP_ID),
                appTransId,
                ""
        );

        try {
            // Chuỗi tạo MAC để truy vấn trạng thái: appid|app_trans_id|key1
            String macData = String.format(new Locale("vi", "vn"), "%d|%s|%s",
                    data.app_id,
                    data.app_trans_id,
                    BuildConfig.ZALO_PAY_KEY1);

            data.mac = MACGenerator.computeMac(macData, BuildConfig.ZALO_PAY_KEY1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return api.queryStatus(data);
    }
}