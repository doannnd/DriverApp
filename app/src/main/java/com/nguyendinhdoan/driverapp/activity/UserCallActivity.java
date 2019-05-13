package com.nguyendinhdoan.driverapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.common.Common;
import com.nguyendinhdoan.driverapp.model.Notification;
import com.nguyendinhdoan.driverapp.model.Result;
import com.nguyendinhdoan.driverapp.model.Sender;
import com.nguyendinhdoan.driverapp.model.Token;
import com.nguyendinhdoan.driverapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.driverapp.remote.IGoogleAPI;
import com.nguyendinhdoan.driverapp.services.MyFirebaseIdServices;
import com.nguyendinhdoan.driverapp.services.MyFirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserCallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final double LATITUDE_DEFAULT = -1;
    private static final double LONGITUDE_DEFAULT = -1;
    private static final String TAG = "USER_CALL_ACTIVITY";
    public static final String DIRECTION_ROUTES_KEY = "routes";
    private static final String DIRECTION_LEGS_KEY = "legs";
    private static final String DIRECTION_DURATION_KEY = "duration";
    private static final String DIRECTION_DISTANCE_KEY = "distance";
    private static final String DIRECTION_ADDRESS_KEY = "end_address";
    private static final String DIRECTION_TEXT_KEY = "text";
    public static final String LAT_USER = "LAT_USER";
    public static final String LNG_USER = "LNG_USER";
    public static final String ID_USER = "ID_USER";

    public static final long COUNT_DOWN_INTERVAL = 1000;
    public static final long INITIAL_COUNT_DOWN = 30000;

    private TextView timeTextView;
    private TextView distanceTextView;
    private TextView addressTextView;
    private Button acceptButton;
    private Button declineButton;
    private TextView timeLeftTextView;

    private MediaPlayer mediaPlayer;
    private IGoogleAPI mGoogleService;
    private IFirebaseMessagingAPI mFirebaseService;

    private String userId;
    private double latitudeUser;
    private double longitudeUser;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_call);

        setupBroadcastReceiver();
        initViews();
        setupUI();
        addEvent();

        setupCountDownTimer();
        updateStateDrivers();
    }

    private void setupBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(MyFirebaseMessaging.MESSAGE_USER_KEY));
    }

    private void updateStateDrivers() {
        // create object update
        Map<String, Object> driverUpdateState = new HashMap<>();
        driverUpdateState.put("state", "working");

        DatabaseReference driverTable = FirebaseDatabase.getInstance().getReference("drivers");
        driverTable.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .updateChildren(driverUpdateState)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserCallActivity.this, "update state driver success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserCallActivity.this, "update state driver failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupCountDownTimer() {
        countDownTimer = new CountDownTimer(INITIAL_COUNT_DOWN, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long timeLeft = millisUntilFinished / 1000;
                timeLeftTextView.setText(String.valueOf(timeLeft));
            }

            @Override
            public void onFinish() {
                if (userId != null) {
                    cancelBooking(userId);
                } else {
                    Toast.makeText(UserCallActivity.this, "user id must be not null", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    private void addEvent() {
        declineButton.setOnClickListener(this);
        acceptButton.setOnClickListener(this);
    }

    private void setupUI() {
        initGoogleService();
        setupMediaPlayer();
        displayCallData();
    }

    private void displayCallData() {
        Intent intentCallDriver = getIntent();
        if (intentCallDriver != null) {
            latitudeUser = intentCallDriver.getDoubleExtra(MyFirebaseMessaging.LATITUDE_KEY, LATITUDE_DEFAULT);
            longitudeUser = intentCallDriver.getDoubleExtra(MyFirebaseMessaging.LONGITUDE_KEY, LONGITUDE_DEFAULT);
            userId = intentCallDriver.getStringExtra(MyFirebaseMessaging.USER_ID_KEY);
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
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
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
                                String address = legObject.getString(DIRECTION_ADDRESS_KEY);
                                addressTextView.setText(address);
                                Log.d(TAG, "address: " + address);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
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
        mFirebaseService = Common.getFirebaseMessagingAPI();
    }

    private void initViews() {
        timeTextView = findViewById(R.id.time_text_view);
        distanceTextView = findViewById(R.id.distance_text_view);
        addressTextView = findViewById(R.id.address_text_view);
        acceptButton = findViewById(R.id.accept_button);
        declineButton = findViewById(R.id.decline_button);
        timeLeftTextView = findViewById(R.id.time_left_text_view);
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

    @Override
    protected void onResume() {
        super.onResume();
        //mediaPlayer.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.decline_button: {
                cancelBooking(userId);
                break;
            }
            case R.id.accept_button: {
                acceptBooking(userId);
                openTrackingActivity();
                break;
            }
        }
    }

    private void acceptBooking(String userId) {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        tokenTable.orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            String bodyMessage = String.format("The driver %s is moving to your location", Common.currentDriver.getName());
                            Notification notification = new Notification("accept", bodyMessage);
                            if (token != null) {
                                Sender sender = new Sender(notification, token.getToken());

                                mFirebaseService.sendMessage(sender)
                                        .enqueue(new Callback<Result>() {
                                            @Override
                                            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                                                if (response.isSuccessful()) {
                                                    Log.d(TAG, "onResponse: success send notification");
                                                }
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                                                Log.e(TAG, "onFailure: error" + t.getMessage());
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled: error" + databaseError);
                    }
                });
    }

    private void openTrackingActivity() {
        Intent intentTracking = new Intent(this, TrackingActivity.class);
        intentTracking.putExtra(LAT_USER, latitudeUser);
        intentTracking.putExtra(LNG_USER, longitudeUser);
        intentTracking.putExtra(ID_USER, userId);
        startActivity(intentTracking);
        finish();
    }

    private void cancelBooking(String userId) {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        tokenTable.orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            String bodyMessage = "Driver cancel booking from user";
                            Notification notification = new Notification("cancel", bodyMessage);
                            if (token != null) {
                                Sender sender = new Sender(notification, token.getToken());

                                mFirebaseService.sendMessage(sender)
                                        .enqueue(new Callback<Result>() {
                                            @Override
                                            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                                                if (response.isSuccessful()) {
                                                    //updateCancelDriver();
                                                    updateStateDriver();
                                                    Toast.makeText(UserCallActivity.this, "cancel booking", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                                                Log.e(TAG, "onFailure: error" + t.getMessage());
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled: error" + databaseError);
                    }
                });
    }

    private void updateStateDriver() {
        Map<String, Object> driverUpdateState = new HashMap<>();
        driverUpdateState.put("state", "not_working");

        DatabaseReference driverTable = FirebaseDatabase.getInstance().getReference("drivers");
        driverTable.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .updateChildren(driverUpdateState)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(UserCallActivity.this, "update state driver success", Toast.LENGTH_SHORT).show();
                            Log.d("update", "update state driver success");
                        } else {
                            Toast.makeText(UserCallActivity.this, "update state driver failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyFirebaseMessaging.MESSAGE_KEY);
            if (MyFirebaseMessaging.CANCEL_TITLE.equals(message)) {
                finish();
            }

        }
    };

    @Override
    protected void onDestroy() {
        countDownTimer.cancel();
        super.onDestroy();
    }
}
