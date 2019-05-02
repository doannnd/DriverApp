package com.nguyendinhdoan.driverapp.services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.nguyendinhdoan.driverapp.activity.UserCallActivity;

/**
 * receive data from user app : driverId and current location user
* */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "MESSAGE_SERVICE";

    public static final String LATITUDE_KEY = "LATITUDE_KEY";
    public static final String LONGITUDE_KEY = "LONGITUDE_KEY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: receive data from user app");
        super.onMessageReceived(remoteMessage);

        // body is current location of user
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "onMessageReceived: if has data");
            String jsonBody = remoteMessage.getNotification().getBody();
            // convert string to LatLng
            LatLng currentLocationUser = new Gson().fromJson(jsonBody, LatLng.class);
            Log.d(TAG, "onMessageReceive: latitude user: " + currentLocationUser.latitude);
            Log.d(TAG, "onMessageReceived: longitude user: " + currentLocationUser.longitude);

            // jump to UserCallActivity to display information caller
            Intent intentCall = new Intent(getBaseContext(), UserCallActivity.class);
            intentCall.putExtra(LATITUDE_KEY, currentLocationUser.latitude);
            intentCall.putExtra(LONGITUDE_KEY, currentLocationUser.longitude);
            intentCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intentCall);
        }

    }
}
