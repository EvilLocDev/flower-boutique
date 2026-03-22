package com.example.flowerboutique.utils.zalopay;

public class RequestCreateZalopayOrderBody {
    public int app_id;
    public String app_user;
    public String app_trans_id;
    public long app_time;
    public long amount;
    public String item;
    public String description;
    public String embed_data;
    public String bank_code;
    public String mac;

    // Constructor rỗng (Bắt buộc phải có để Gson parse dữ liệu)
    public RequestCreateZalopayOrderBody() {
    }

    public RequestCreateZalopayOrderBody(int app_id, String app_user, String app_trans_id, long app_time, long amount, String item, String description, String embed_data, String bank_code, String mac) {
        this.app_id = app_id;
        this.app_user = app_user;
        this.app_trans_id = app_trans_id;
        this.app_time = app_time;
        this.amount = amount;
        this.item = item;
        this.description = description;
        this.embed_data = embed_data;
        this.bank_code = bank_code;
        this.mac = mac;
    }
}