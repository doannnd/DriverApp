package com.nguyendinhdoan.driverapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.activity.UserCallActivity;
import com.nguyendinhdoan.driverapp.common.Common;
import com.nguyendinhdoan.driverapp.model.Body;
import com.nguyendinhdoan.driverapp.utils.NotificationUtils;

import java.util.Objects;

/**
 * receive data from user app : driverId and current location user
 */
public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static final String LATITUDE_KEY = "LATITUDE_KEY";
    public static final String LONGITUDE_KEY = "LONGITUDE_KEY";
    public static final String USER_ID_KEY = "USER_ID_KEY";
    public static final String CANCEL_TITLE = "cancel";
    public static final String CANCEL_TRIP_TITLE = "cancelTrip";

    private static final int PENDING_REQUEST_CODE = 0;
    private static final int NOTIFY_ID = 1;
    public static final String MESSAGE_USER_KEY = "MESSAGE_USER_KEY";
    public static final String MESSAGE_KEY = "MESSAGE_KEY";
    public static final String MESSAGE_TRACKING_KEY = "MESSAGE_TRACKING_KEY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String jsonBody = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();

            if (Objects.requireNonNull(title).equals(CANCEL_TITLE)) {
                sendMessageToUserCallActivity(title);
                notification(title, jsonBody);
            } else if (Objects.requireNonNull(title).equals(CANCEL_TRIP_TITLE)) {
                sendMessageToTrackingActivity(title);
                notification(title, jsonBody);
            }else {
                handleReceiveCallFromUser(jsonBody, title);
            }
        }

    }

    private void sendMessageToTrackingActivity(String message) {
        Intent intent = new Intent(MESSAGE_TRACKING_KEY);
        intent.putExtra(MESSAGE_KEY, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageToUserCallActivity(String message) {
        Intent intent = new Intent(MESSAGE_USER_KEY);
        intent.putExtra(MESSAGE_KEY, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notification(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showArrivedNotificationAPI26(title, body);
        } else {
            showArrivedNotification(title, body);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String title, String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(
                getBaseContext(), PENDING_REQUEST_CODE,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationUtils notificationUtils = new NotificationUtils(getBaseContext());
        Notification.Builder builder = notificationUtils.getUberNotification(
                title, body, contentIntent, defaultSound
        );

        notificationUtils.getManager().notify(NOTIFY_ID, builder.build());
    }

    private void showArrivedNotification(String title, String body) {
        PendingIntent contentIntent = PendingIntent.getActivity(
                getBaseContext(), PENDING_REQUEST_CODE,
                new Intent(), PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_go_location)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getBaseContext()
                .getSystemService(NOTIFICATION_SERVICE);
        Objects.requireNonNull(manager).notify(NOTIFY_ID, builder.build());
    }

    private void handleReceiveCallFromUser(String jsonBody, String title) {
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
