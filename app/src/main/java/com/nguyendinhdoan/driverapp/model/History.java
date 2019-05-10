package com.nguyendinhdoan.driverapp.model;

public class History {
    private String date;
    private String startAddress;
    private String endAddress;
    private String distance;
    private String time;

    public History(String date, String startAddress, String endAddress, String distance, String time) {
        this.date = date;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.distance = distance;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
