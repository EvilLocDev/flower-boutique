package com.example.flowerboutique.utils.zalopay;

import com.google.gson.annotations.SerializedName;

public class ResponseCreateZalopayOrderBody {
    public int return_code;
    public String return_message;
    public int sub_return_code;
    public String sub_return_message;

    @SerializedName("zp_trans_token")
    public String zpTransToken;

    public String order_url;
    public String order_token;

    public int getReturnCode() {
        return return_code;
    }

    public String getReturnMessage() {
        return return_message;
    }

    public String getZpTransToken() {
        return zpTransToken;
    }

    public String getSubReturnMessage() {
        return sub_return_message;
    }

    public String getOrderUrl() {
        return order_url;
    }
}
