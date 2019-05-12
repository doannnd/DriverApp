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

    public Driver() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.phone);
        dest.writeString(this.avatarUrl);
        dest.writeString(this.rates);
        dest.writeString(this.licensePlates);
        dest.writeString(this.vehicleName);
        dest.writeString(this.zeroToTwo);
        dest.writeString(this.threeToTen);
        dest.writeString(this.elevenToTwenty);
        dest.writeString(this.biggerTwenty);
    }

    protected Driver(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
        this.avatarUrl = in.readString();
        this.rates = in.readString();
        this.licensePlates = in.readString();
        this.vehicleName = in.readString();
        this.zeroToTwo = in.readString();
        this.threeToTen = in.readString();
        this.elevenToTwenty = in.readString();
        this.biggerTwenty = in.readString();
    }

    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
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
