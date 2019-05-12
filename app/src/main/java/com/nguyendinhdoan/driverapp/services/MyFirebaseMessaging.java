package com.nguyendinhdoan.driverapp.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.nguyendinhdoan.driverapp.activity.UserCallActivity;
import com.nguyendinhdoan.driverapp.common.Common;
import com.nguyendinhdoan.driverapp.model.Body;

/**
 * receive data from user app : driverId and current location user
 */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static final String LATITUDE_KEY = "LATITUDE_KEY";
    public static final String LONGITUDE_KEY = "LONGITUDE_KEY";
    public static final String USER_ID_KEY = "USER_ID_KEY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String jsonBody = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();

            Body body = new Gson().fromJson(jsonBody, Body.class);
            LatLng currentLocationUser = body.getCurrentLocationUser();
            Common.destinationLocationUser = body.getDestinationLocationUser();
            Common.userDestination = body.getUserDestination();

            // jump to UserCallActivity to display information caller
            Intent intentCall = new Intent(getBaseContext(), UserCallActivity.class);
            intentCall.putExtra(LATITUDE_KEY, currentLocationUser.latitude);
            intentCall.putExtra(LONGITUDE_KEY, currentLocationUser.longitude);
            intentCall.putExtra(USER_ID_KEY, title);
            intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intentCall);
        }

    }
}
