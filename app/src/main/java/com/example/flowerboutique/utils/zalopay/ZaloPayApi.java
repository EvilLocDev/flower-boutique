package com.example.flowerboutique.utils.zalopay;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ZaloPayApi {

    // API xin cấp Token cho đơn hàng
    @POST("v2/create")
    Call<ResponseCreateZalopayOrderBody> createOrder(@Body RequestCreateZalopayOrderBody body);

    // API truy vấn xem khách đã thanh toán hay chưa (nếu cần thiết)
    @POST("v2/query")
    Call<Object> queryStatus(@Body RequestQueryTransStatus body);
}