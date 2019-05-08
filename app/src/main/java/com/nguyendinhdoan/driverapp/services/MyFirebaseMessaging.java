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
* */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "MESSAGE_SERVICE";

    public static final String LATITUDE_KEY = "LATITUDE_KEY";
    public static final String LONGITUDE_KEY = "LONGITUDE_KEY";
    public static final String USER_ID_KEY = "USER_ID_KEY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: receive data from user app");
        super.onMessageReceived(remoteMessage);

        // body is current location of user
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "onMessageReceived: if has data");
            String jsonBody = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();
            // convert string to LatLng
            LatLng currentLocationUser;
            Body body;
            if (jsonBody.contains("destinationLocationUser")) {
                body = new Gson().fromJson(jsonBody, Body.class);
                currentLocationUser = body.getCurrentLocationUser();
                Common.destinationLocationUser = body.getDestinationLocationUser();
                Common.userDestination = body.getUserDestination();
                Log.d(TAG, "destination location user" + Common.destinationLocationUser);
                Log.d(TAG, "user destination: " + Common.userDestination);
            } else {
                currentLocationUser = new Gson().fromJson(jsonBody, LatLng.class);
                Common.destinationLocationUser = null;
                Common.userDestination = null;
            }

            Log.d(TAG, "onMessageReceive: latitude user: " + currentLocationUser.latitude);
            Log.d(TAG, "onMessageReceived: longitude user: " + currentLocationUser.longitude);
            Log.d(TAG, "onMessageReceived: user id : " + title);

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
