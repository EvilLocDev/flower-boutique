package com.example.flowerboutique;

import java.util.Date;
import java.util.HashMap;
import java.util.PrimitiveIterator;

// class thong tin nguoi dung
public class UserInfor {
    private String uid;
    private String name;
    private String phongNumber;
    private String gerder;
    private String role;
    private String birthday;
    private String avatar;
    private String status;
    private String created_date;
    private String updated_date;

    public UserInfor(){

    }

    public UserInfor(String uid, String name, String role) {
        this.uid = uid;
        this.name = name;
        this.role = role;
    }

    public UserInfor(String uid,String name, String phongNumber, String gerder, String role, String birthday, String avatar, String status, String created_date, String updated_date) {
        this.uid = uid;
        this.name = name;
        this.phongNumber = phongNumber;
        this.gerder = gerder;
        this.role = role;
        this.birthday = birthday;
        this.avatar = avatar;
        this.status = status;
        this.created_date = created_date;
        this.updated_date = updated_date;
    }

    public HashMap<String,Object> convertToHashmap(){
        HashMap<String,Object> m= new HashMap<>();
        m.put("uid",uid);
        m.put("name",name);
        m.put("gender",gerder);
        m.put("role",role);
        m.put("phoneNumber",phongNumber);
        m.put("birthday",birthday);
        m.put("avatar",avatar);
        m.put("status",status);
        m.put("created_date",created_date);
        m.put("updated_date",updated_date);
        return m;
    }



    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhongNumber() {
        return phongNumber;
    }

    public void setPhongNumber(String phongNumber) {
        this.phongNumber = phongNumber;
    }

    public String getGerder() {
        return gerder;
    }

    public void setGerder(String gerder) {
        this.gerder = gerder;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getUpdated_date() {
        return updated_date;
    }

    public void setUpdated_date(String updated_date) {
        this.updated_date = updated_date;
    }
}
