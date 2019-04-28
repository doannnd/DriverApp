package com.nguyendinhdoan.driverapp.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.common.Common;
import com.nguyendinhdoan.driverapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.driverapp.remote.IGoogleAPI;
import com.nguyendinhdoan.driverapp.services.MessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserCallActivity extends AppCompatActivity {

    private static final double LATITUDE_DEFAULT = -1;
    private static final double LONGITUDE_DEFAULT = -1;
    private static final String TAG = "USER_CALL_ACTIVITY";
    public static final String DIRECTION_ROUTES_KEY = "routes";
    private static final String DIRECTION_LEGS_KEY = "legs";
    private static final String DIRECTION_DURATION_KEY = "duration";
    private static final String DIRECTION_DISTANCE_KEY = "distance";
    private static final String DIRECTION_ADDRESS_KEY = "end_address";
    private static final String DIRECTION_TEXT_KEY = "text";

    private ImageView mapImageView;
    private TextView timeTextView;
    private TextView distanceTextView;
    private TextView addressTextView;

    private MediaPlayer mediaPlayer;
    private IGoogleAPI mGoogleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_call);

        initViews();
        setupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    private void setupUI() {
        initGoogleService();
        setupMediaPlayer();
        displayCallData();
    }

    private void displayCallData() {
        Intent intentCallDriver = getIntent();
        if (intentCallDriver != null) {
            double latitudeUser = intentCallDriver.getDoubleExtra(MessagingService.LATITUDE_KEY, LATITUDE_DEFAULT);
            double longitudeUser = intentCallDriver.getDoubleExtra(MessagingService.LONGITUDE_KEY, LONGITUDE_DEFAULT);
            Log.d(TAG, "latitude user: " + latitudeUser);
            Log.d(TAG, "longitude user: " + longitudeUser);

            updateUIUserCall(latitudeUser, longitudeUser);
        }
    }

    private void updateUIUserCall(double latitudeUser, double longitudeUser) {
        try {
            LatLng destinationLocation = new LatLng(latitudeUser, longitudeUser);
            LatLng originLocation = new LatLng(Common.currentLocation.getLatitude(), Common.currentLocation.getLongitude());
            String userCallURL = Common.directionURL(originLocation, destinationLocation);

            mGoogleService.getDirectionPath(userCallURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                            try {
                                JSONObject root = new JSONObject(response.body());
                                JSONArray routes = root.getJSONArray(DIRECTION_ROUTES_KEY);
                                JSONObject routeObject = routes.getJSONObject(0);
                                JSONArray legs = routeObject.getJSONArray(DIRECTION_LEGS_KEY);
                                JSONObject legObject = legs.getJSONObject(0);

                                // get time and display on time text view
                                JSONObject time = legObject.getJSONObject(DIRECTION_DURATION_KEY);
                                timeTextView.setText(time.getString(DIRECTION_TEXT_KEY));
                                Log.d(TAG, "time: " + time.getString(DIRECTION_TEXT_KEY));

                                // get distance and display on distance text view
                                JSONObject distance = legObject.getJSONObject(DIRECTION_DISTANCE_KEY);
                                distanceTextView.setText(distance.getString(DIRECTION_TEXT_KEY));
                                Log.d(TAG, "distance: " + distance.getString(DIRECTION_TEXT_KEY));


                                // get address and display on address text view
                                JSONObject address = legObject.getJSONObject(DIRECTION_ADDRESS_KEY);
                                addressTextView.setText(address.getString(DIRECTION_TEXT_KEY));
                                Log.d(TAG, "address: " + address.getString(DIRECTION_TEXT_KEY));



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                            Log.e(TAG, "error load information user : time, distance, address");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void initGoogleService() {
        mGoogleService = Common.getGoogleAPI();
    }

    private void initViews() {
        mapImageView = findViewById(R.id.map_image_view);
        timeTextView = findViewById(R.id.time_text_view);
        distanceTextView = findViewById(R.id.distance_text_view);
        addressTextView = findViewById(R.id.address_text_view);
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }
}
