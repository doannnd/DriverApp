package com.nguyendinhdoan.driverapp.model;

public class History {

   private String startAddress;
   private String endAddress;
   private String dateTime;
   private String tripPrice;

    public History() {
    }

    public History(String startAddress, String endAddress, String dateTime, String tripPrice) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.dateTime = dateTime;
        this.tripPrice = tripPrice;
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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTripPrice() {
        return tripPrice;
    }

    public void setTripPrice(String tripPrice) {
        this.tripPrice = tripPrice;
    }
}
