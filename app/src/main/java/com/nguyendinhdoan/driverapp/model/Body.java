package com.nguyendinhdoan.driverapp.model;

import com.google.android.gms.maps.model.LatLng;

public class Body {
    private LatLng currentLocationUser;
    private LatLng destinationLocationUser;
    private String userDestination;

    public Body(LatLng currentLocationUser, LatLng destinationLocationUser, String userDestination) {
        this.currentLocationUser = currentLocationUser;
        this.destinationLocationUser = destinationLocationUser;
        this.userDestination = userDestination;
    }

    public String getUserDestination() {
        return userDestination;
    }

    public void setUserDestination(String userDestination) {
        this.userDestination = userDestination;
    }

    public LatLng getCurrentLocationUser() {
        return currentLocationUser;
    }

    public void setCurrentLocationUser(LatLng currentLocationUser) {
        this.currentLocationUser = currentLocationUser;
    }

    public LatLng getDestinationLocationUser() {
        return destinationLocationUser;
    }

    public void setDestinationLocationUser(LatLng destinationLocationUser) {
        this.destinationLocationUser = destinationLocationUser;
    }
}
