package com.example.flowerboutique.utils.zalopay;

import com.google.gson.annotations.SerializedName;

public class ResponseCreateZalopayOrderBody {
    public int return_code;
    public String return_message;
    public int sub_return_code;
    public String sub_return_message;

    // Annotation này giúp map đúng key "zp_trans_token" từ JSON của ZaloPay trả về
    @SerializedName("zp_trans_token")
    public String zpTransToken;

    public String order_url;
    public String order_token;
}