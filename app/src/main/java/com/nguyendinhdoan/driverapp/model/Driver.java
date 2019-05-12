package com.nguyendinhdoan.driverapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Driver implements Parcelable {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private String rates;
    private String licensePlates;
    private String vehicleName;
    private String zeroToTwo;
    private String threeToTen;
    private String elevenToTwenty;
    private String biggerTwenty;

    public Driver(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Driver(String id, String name, String email, String phone, String licensePlates,
                  String vehicleName, String zeroToTwo, String threeToTen, String elevenToTwenty, String biggerTwenty) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.licensePlates = licensePlates;
        this.vehicleName = vehicleName;
        this.zeroToTwo = zeroToTwo;
        this.threeToTen = threeToTen;
        this.elevenToTwenty = elevenToTwenty;
        this.biggerTwenty = biggerTwenty;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicensePlates() {
        return licensePlates;
    }

    public void setLicensePlates(String licensePlates) {
        this.licensePlates = licensePlates;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getZeroToTwo() {
        return zeroToTwo;
    }

    public void setZeroToTwo(String zeroToTwo) {
        this.zeroToTwo = zeroToTwo;
    }

    public String getThreeToTen() {
        return threeToTen;
    }

    public void setThreeToTen(String threeToTen) {
        this.threeToTen = threeToTen;
    }

    public String getElevenToTwenty() {
        return elevenToTwenty;
    }

    public void setElevenToTwenty(String elevenToTwenty) {
        this.elevenToTwenty = elevenToTwenty;
    }

    public String getBiggerTwenty() {
        return biggerTwenty;
    }

    public void setBiggerTwenty(String biggerTwenty) {
        this.biggerTwenty = biggerTwenty;
    }

    public static Creator<Driver> getCREATOR() {
        return CREATOR;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.phone);
    }

    public Driver() {
    }

    protected Driver(Parcel in) {
        this.name = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
    }

    public static final Parcelable.Creator<Driver> CREATOR = new Parcelable.Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel source) {
            return new Driver(source);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

}
