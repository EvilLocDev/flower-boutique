package com.example.flowerboutique.utils.zalopay;

public class RequestQueryTransStatus {
    public int app_id;
    public String app_trans_id;
    public String mac;

    public RequestQueryTransStatus(int app_id, String app_trans_id, String mac) {
        this.app_id = app_id;
        this.app_trans_id = app_trans_id;
        this.mac = mac;
    }
}