package com.example.flowerboutique.db.entities;

public class AddressModel {
    private String address;
    private String city;
    private String district;
    private String ward;

    public AddressModel() {
    }

    public AddressModel(String address, String city, String district, String ward) {
        this.address = address;
        this.city = city;
        this.district = district;
        this.ward = ward;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getWard() {
        return ward;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }
}