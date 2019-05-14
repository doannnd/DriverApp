package com.nguyendinhdoan.driverapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.common.Common;
import com.nguyendinhdoan.driverapp.model.Driver;
import com.nguyendinhdoan.driverapp.model.History;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "TrackingActivity";
    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    public static final float DRIVER_MAP_ZOOM = 15.0F;
    public static final String DIRECTION_ROUTES_KEY = "routes";
    public static final String DIRECTION_POLYLINE_KEY = "overview_polyline";
    public static final String DIRECTION_POINT_KEY = "points";
    public static final int DIRECTION_PADDING = 150;
    private static final float POLYLINE_WIDTH = 8F;
    private static final double CIRCLE_RADIUS = 50; // 50m
    private static final float CIRCLE_STROKE_WIDTH = 5.0F;
    private static final int CIRCLE_FILL_COLOR = 0x220000FF;

    private static final String DIRECTION_LEGS_KEY = "legs";
    private static final String DIRECTION_DISTANCE_KEY = "distance";
    private static final String DIRECTION_ADDRESS_KEY = "end_address";
    private static final String DIRECTION_TEXT_KEY = "text";

    public static final String LOCATION_END_INTENT_KEY = "LOCATION_END_INTENT_KEY";
    private static final String USER_TABLE_NAME = "users";
    public static final String PICKUP_REQUEST_TABLE_NAME = "pickup_request";
    public static final double USER_RADIUS = 0.05;
    public static final int BOTTOM_MAP = 30;
    public static final int RIGHT_MAP = 30;
    public static final int TOP_MAP = 0;
    public static final int LEFT_MAP = 0;
    public static final int SUBJECT_KEY = 0;
    public static final String TRIP_PRICE_INTENT_KEY = "TRIP_PRICE_INTENT_KEY";
    public static final String HISTORY_DRIVER_TABLE_NAME = "history_driver";
    public static final String HISTORY_USER_TABLE_NAME = "history_user";
    public static final String START_ADDRESS_KEY = "start_address";
    public static final String END_ADDRESS_KEY = "end_address";
    public static final int INDEX_ROUTE = 0;
    public static final int INDEX_LEG = 0;
    public static final String CANCEL_TRIP_TITLE = "cancelTrip";
    public static final String STATE_KEY = "state";
    public static final String DRIVER_TABLE_NAME = "drivers";
    public static final String START_TRIP_KEY = "startTrip";
    public static final String DROP_OFF_TITLE = "DropOff";
    public static final String ARRIVED_TITLE = "Arrived";
    public static final String END_ADDRESS_INTENT_KEY = "END_ADDRESS_INTENT_KEY";

    private ProgressBar loadingProgressBar;
    private GoogleMap mTrackingMap;
    private Button startTripButton;
    private Toolbar trackingToolbar;
    private TextView destinationTextView;
    private FloatingActionButton directionButton;
    private CircleImageView avatarImageView;

    private TextView userNameTextView;
    private ImageView phoneImageView;
    private Button cancelTripButton;

    private String userId;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Polyline trackingPolyline;

    private IGoogleAPI mServices;
    private IFirebaseMessagingAPI mFirebaseService;
    private List<LatLng> directionPolylineList;

    private Location pickupLocation;
    private String unitDistance;
    private String phoneNumberUser;
    private GeoLocation userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        setupBroadcastReceiver();
        initViews();
        initGoogleMap();
        setupUI();
        addEvents();
    }

    private void setupBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(MyFirebaseMessaging.MESSAGE_TRACKING_KEY));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvents() {
        startTripButton.setOnClickListener(this);
        directionButton.setOnClickListener(this);
        phoneImageView.setOnClickListener(this);
        cancelTripButton.setOnClickListener(this);
    }

    private void initViews() {
        trackingToolbar = findViewById(R.id.tracking_toolbar);
        setupToolbar();

        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        startTripButton = findViewById(R.id.start_trip_button);
        directionButton = findViewById(R.id.direction_button);
        destinationTextView = findViewById(R.id.destination_text_view);

        View view = findViewById(R.id.user_detail_bottom_sheet);

        avatarImageView = view.findViewById(R.id.avatar_image_view);
        userNameTextView = view.findViewById(R.id.user_name_text_view);
        phoneImageView = view.findViewById(R.id.phone_image_view);
        cancelTripButton = view.findViewById(R.id.cancel_trip_button);
    }

    private void setupToolbar() {
        setSupportActionBar(trackingToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupUI() {
        if (getIntent() != null) {
            userId = getIntent().getStringExtra(UserCallActivity.ID_USER);
            showUserDetail(userId);
        }
        // initial fused location provider
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        mServices = Common.getGoogleAPI();
        mFirebaseService = Common.getFirebaseMessagingAPI();

        // update current location of driver
        startLocationUpdates();
    }

    private void initGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trip_detail_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // add my button location in bottom right
        View locationButton = ((View) Objects.requireNonNull(Objects.requireNonNull(mapFragment).getView())
                .findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, SUBJECT_KEY);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(LEFT_MAP, TOP_MAP, RIGHT_MAP, BOTTOM_MAP);
    }

    private void startLocationUpdates() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buildLocationRequest();
                            buildLocationCallback();
                            // update location
                            if (ActivityCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest, locationCallback, Looper.myLooper());
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();

    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Common.currentLocation = locationResult.getLastLocation();
                Log.d(TAG, "current location latitude: " + Common.currentLocation.getLatitude());
                Log.d(TAG, "current location longitude: " + Common.currentLocation.getLongitude());

                // display current location on the google map
                displayCurrentLocation();
            }
        };

    }

    private void displayCurrentLocation() {
        if (Common.currentLocation != null) {
            final double driverLatitude = Common.currentLocation.getLatitude();
            final double driverLongitude = Common.currentLocation.getLongitude();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mTrackingMap.setMyLocationEnabled(true);
            mTrackingMap.getUiSettings().setMyLocationButtonEnabled(true);

            // move camera
            mTrackingMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(new LatLng(driverLatitude, driverLongitude), DRIVER_MAP_ZOOM)
            );

            loadingProgressBar.setVisibility(View.GONE);

            if (trackingPolyline != null) {
                trackingPolyline.remove();
            }

            displayUserLocation();
        }
    }

    private void displayUserLocation() {
        mTrackingMap.clear();

        if (userId != null) {
            DatabaseReference pickupRequestTable = FirebaseDatabase.getInstance().getReference(PICKUP_REQUEST_TABLE_NAME);
            GeoFire driverLocationGeoFire = new GeoFire(pickupRequestTable);
            driverLocationGeoFire.getLocation(userId, new com.firebase.geofire.LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (location != null) {
                        userLocation = location;
                        // show user with icon car on google map
                        mTrackingMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude, location.longitude))
                                .icon(BitmapDescriptorFactory.defaultMarker())
                        );

                        // draw  the circle with radius = 50m
                        mTrackingMap.addCircle(new CircleOptions()
                                .center(new LatLng(location.latitude, location.longitude))
                                .radius(CIRCLE_RADIUS)
                                .strokeColor(ContextCompat.getColor(TrackingActivity.this, R.color.blue_light))
                                .strokeWidth(CIRCLE_STROKE_WIDTH)
                                .fillColor(CIRCLE_FILL_COLOR)
                        );

                        handleDriverDirection(location);
                        handleGeoFencing(location);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "error load location of driver: " + databaseError);
                }
            });
        }
    }

    private void handleDriverDirection(GeoLocation location) {
        // save current position
        double currentLatitude = Common.currentLocation.getLatitude();
        double currentLongitude = Common.currentLocation.getLongitude();
        LatLng currentPosition = new LatLng(currentLatitude, currentLongitude);
        LatLng destinationLocation = new LatLng(location.latitude, location.longitude);

        try {
            //building direction url for driver
            String directionURL = Common.directionURL(currentPosition, destinationLocation);
            Log.d(TAG, "direction url: " + directionURL);

            // show direction
            mServices.getDirectionPath(directionURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            handleDirectionJSON(response.body());
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Log.e(TAG, "error in show direction of driver: " + t.getMessage());
                            showSnackBar(t.getMessage());
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleDirectionJSON(String directionJSON) {
        try {
            JSONObject root = new JSONObject(directionJSON);
            JSONArray routes = root.getJSONArray(DIRECTION_ROUTES_KEY);
            // display end start on edit text
            JSONObject routeObject = routes.getJSONObject(0);
            JSONArray legs = routeObject.getJSONArray(DIRECTION_LEGS_KEY);
            JSONObject legObject = legs.getJSONObject(0);

            JSONObject distance = legObject.getJSONObject(DIRECTION_DISTANCE_KEY);
            String km = distance.getString(DIRECTION_TEXT_KEY);
            Log.d(TAG, "km: " + distance.getString(DIRECTION_TEXT_KEY));

            double distanceUserAndDriver = Double.parseDouble(km.replaceAll("[^0-9\\\\.]", ""));
            String[] units = km.split(" ");
            unitDistance = units[1];
            Log.d(TAG, "distance user and driver: " + distanceUserAndDriver);
            Log.d(TAG, "unit distance: " + unitDistance);

            String userAddress = legObject.getString(DIRECTION_ADDRESS_KEY);
            destinationTextView.setText(userAddress);

            // handle and decode direction json ==> string
            for (int i = 0; i < routes.length(); i++) {
                JSONObject route = routes.getJSONObject(i);
                JSONObject overviewPolyline = route.getJSONObject(DIRECTION_POLYLINE_KEY);
                String points = overviewPolyline.getString(DIRECTION_POINT_KEY);
                directionPolylineList = PolyUtil.decode(points);
            }
            Log.d(TAG, "direction polyline list size: " + directionPolylineList.size());

            // show direction polyline on google map
            showDirectionOnMap(directionPolylineList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDirectionOnMap(List<LatLng> directionPolylineList) {
        // adjusting bound
        if (unitDistance.equals("km")) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : directionPolylineList) {
                builder.include(latLng);
            }

            // handle display camera
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, DIRECTION_PADDING);
            mTrackingMap.moveCamera(cameraUpdate);
        }

        // handle information display of direction gray polyline
        PolylineOptions grayPolylineOptions = new PolylineOptions();
        grayPolylineOptions.color(ContextCompat.getColor(this, R.color.colorBackgroundUserCall));
        grayPolylineOptions.width(POLYLINE_WIDTH);
        grayPolylineOptions.startCap(new SquareCap());
        grayPolylineOptions.endCap(new SquareCap());
        grayPolylineOptions.jointType(JointType.ROUND);
        grayPolylineOptions.addAll(directionPolylineList);

        // display black polyline overlay gray polyline on google map
        trackingPolyline = mTrackingMap.addPolyline(grayPolylineOptions);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_REQUEST_DISPLACEMENT);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mTrackingMap = googleMap;
    }

    private void handleGeoFencing(GeoLocation location) {
        DatabaseReference driverLocationTable = FirebaseDatabase
                .getInstance().getReference(DriverActivity.DRIVER_LOCATION_TABLE_NAME);
        GeoFire trackingGeoFire = new GeoFire(driverLocationTable);

        GeoQuery trackingGeoQuery = trackingGeoFire.queryAtLocation(
                new GeoLocation(location.latitude, location.longitude),
                USER_RADIUS // 50m
        );

        trackingGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // if driver in radius = 50m --> notification for user ....
                sendNotificationArrivedToUser(userId);
                startTripButton.setEnabled(true);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendNotificationArrivedToUser(String userId) {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        tokenTable.orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            String bodyMessage = String.format(getString(R.string.arrived_message),
                                    Common.currentDriver.getName());
                            Notification notification = new Notification(ARRIVED_TITLE, bodyMessage);
                            if (token != null) {
                                Sender sender = new Sender(notification, token.getToken());

                                mFirebaseService.sendMessage(sender)
                                        .enqueue(new Callback<Result>() {
                                            @Override
                                            public void onResponse(@NonNull Call<Result> call,
                                                                   @NonNull Response<Result> response) {
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

    private void sendDropOffNotification(final String tripPrice) {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        tokenTable.orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            Notification notification = new Notification(DROP_OFF_TITLE, tripPrice);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_trip_button) {
            if (startTripButton.getText().equals(getString(R.string.start_trip_button_text))) {
                if (Common.userDestination != null && Common.destinationLocationUser != null) {
                    destinationTextView.setText(Common.userDestination);
                }
                pickupLocation = Common.currentLocation;
                cancelTripButton.setVisibility(View.GONE);
                startTripButton.setText(getString(R.string.drop_off_here));

                // send message start trip to user
                startTripButton();

            } else if (startTripButton.getText().equals(getString(R.string.drop_off_here))) {
                calculateCashFee(pickupLocation, Common.currentLocation);
            }
        } else if (v.getId() == R.id.direction_button) {
            if (startTripButton.isEnabled() && Common.destinationLocationUser != null) {
                openDirectionGoogleMap(Common.destinationLocationUser.latitude,
                        Common.destinationLocationUser.longitude);
            } else {
                openDirectionGoogleMap(userLocation.latitude, userLocation.longitude);
            }
        } else if (v.getId() == R.id.phone_image_view) {
            if (phoneNumberUser != null) {
                callUser(phoneNumberUser);
            }
        } else if (v.getId() == R.id.cancel_trip_button) {
            cancelTripButton();
        }
    }

    private void startTripButton() {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        tokenTable.orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            Notification notification = new Notification(
                                    START_TRIP_KEY, getString(R.string.start_trip_message));
                            if (token != null) {
                                Sender sender = new Sender(notification, token.getToken());

                                mFirebaseService.sendMessage(sender)
                                        .enqueue(new Callback<Result>() {
                                            @Override
                                            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                                                if (response.isSuccessful()) {
                                                    //updateCancelDrivers();
                                                    updateStateDrivers();
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

    private void openDirectionGoogleMap(double latitude, double longitude) {
        String uriDirectionWithGoogleMap = "google.navigation:q=" + latitude + "," + longitude;
        Uri gmmIntentUri = Uri.parse(uriDirectionWithGoogleMap);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void updateStateDrivers() {
        Map<String, Object> driverUpdateState = new HashMap<>();
        driverUpdateState.put(STATE_KEY, getString(R.string.state_not_working));

        DatabaseReference driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME);
        driverTable.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .updateChildren(driverUpdateState)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "UPDATE SATE DRIVERS SUCCESS");
                        } else {
                            Log.e(TAG, "UPDATE STATE DRIVERS FAILED");
                        }
                    }
                });
    }

    private void cancelTripButton() {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        tokenTable.orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            Notification notification = new Notification(
                                    CANCEL_TRIP_TITLE, getString(R.string.cancel_trip_message));
                            if (token != null) {
                                Sender sender = new Sender(notification, token.getToken());

                                mFirebaseService.sendMessage(sender)
                                        .enqueue(new Callback<Result>() {
                                            @Override
                                            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                                                if (response.isSuccessful()) {
                                                    //updateCancelDrivers();
                                                    updateStateDrivers();
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


    private void callUser(final String userPhoneNumber) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (ActivityCompat.checkSelfPermission(TrackingActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Intent intentCall = new Intent(Intent.ACTION_CALL);
                        intentCall.setData(Uri.parse("tel:" + userPhoneNumber));
                        startActivity(intentCall);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        showSnackBar(getString(R.string.permission_denied));
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showUserDetail(String userId) {
        if (userId != null) {
            DatabaseReference userTable = FirebaseDatabase.getInstance()
                    .getReference(USER_TABLE_NAME)
                    .child(userId);
            userTable.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Driver userPickupRequest = dataSnapshot.getValue(Driver.class);
                    if (userPickupRequest != null) {
                        // load image
                        Glide.with(TrackingActivity.this)
                                .load(userPickupRequest.getAvatarUrl())
                                .placeholder(R.drawable.ic_profile)
                                .into(avatarImageView);

                        userNameTextView.setText(userPickupRequest.getName());
                        phoneNumberUser = userPickupRequest.getPhone();
                        // star user here
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "load information of user error " + databaseError);
                }
            });
        }
    }

    private void calculateCashFee(final Location pickupLocation, Location currentLocation) {
        try {
            String calculateURL = Common.directionURL(
                    new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude())
                    , new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

            mServices.getDirectionPath(calculateURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            try {
                                JSONObject root = new JSONObject(response.body());
                                JSONArray routes = root.getJSONArray(DIRECTION_ROUTES_KEY);
                                JSONObject routeObject = routes.getJSONObject(INDEX_ROUTE);
                                JSONArray legs = routeObject.getJSONArray(DIRECTION_LEGS_KEY);
                                JSONObject legObject = legs.getJSONObject(INDEX_LEG);

                                // get distance and display on distance text view
                                JSONObject distance = legObject.getJSONObject(DIRECTION_DISTANCE_KEY);
                                String km = distance.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "km: " + distance.getString(DIRECTION_TEXT_KEY));

                                int priceTrip = calculateTripFee(km);
                                sendDropOffNotification(String.valueOf(priceTrip));

                                String startAddress = legObject.getString(START_ADDRESS_KEY);
                                String endAddress = legObject.getString(END_ADDRESS_KEY);
                                String dateTime = Common.getCurrentDate();
                                String tripPrice = String.valueOf(priceTrip);

                                saveHistoryDriver(startAddress, endAddress, dateTime, tripPrice);
                                saveHistoryUser(startAddress, endAddress, dateTime, tripPrice);

                                Intent intentEndGame = new Intent(
                                        TrackingActivity.this, EndGameActivity.class);
                                intentEndGame.putExtra(TRIP_PRICE_INTENT_KEY, String.valueOf(priceTrip));
                                intentEndGame.putExtra(LOCATION_END_INTENT_KEY, new LatLng(
                                        Common.currentLocation.getLatitude(),
                                        Common.currentLocation.getLongitude()));
                                intentEndGame.putExtra(END_ADDRESS_INTENT_KEY, endAddress);
                                intentEndGame.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intentEndGame);
                                finish();


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

    private void saveHistoryUser(String startAddress, String endAddress, String dateTime, String tripPrice) {
        DatabaseReference historyUserTable = FirebaseDatabase.getInstance().getReference(HISTORY_USER_TABLE_NAME);
        History history = new History(startAddress, endAddress, dateTime, tripPrice);
        historyUserTable.child(userId)
                .push()
                .setValue(history)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "save history user success");
                        } else {
                            Log.d(TAG, "save history user failed");
                        }
                    }
                });
    }

    private void saveHistoryDriver(String startAddress, String endAddress, String dateTime, String tripPrice) {
        DatabaseReference historyDriverTable = FirebaseDatabase.getInstance().getReference(HISTORY_DRIVER_TABLE_NAME);
        History historyDriver = new History(startAddress, endAddress, dateTime, tripPrice);
        historyDriverTable.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .push()
                .setValue(historyDriver)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "save history driver success");
                        } else {
                            Log.d(TAG, "save history driver failed");
                        }
                    }
                });
    }

    private int calculateTripFee(String distance) {
        Log.d(TAG, "distance: " + distance);
        String[] distances = distance.split(" ");
        String valueDistance = distances[0];
        String unitDistance = distances[1];

        switch (unitDistance) {
            case "m": {
                double price = Double.parseDouble(Common.currentDriver.getZeroToTwo());
                return (int) (price / 1000);
            }
            case "km": {
                double valueDistanceFormat = Double.parseDouble(valueDistance);
                if (valueDistanceFormat <= 2) {
                    double price = valueDistanceFormat * Double.parseDouble(Common.currentDriver.getZeroToTwo());
                    return (int) (price / 1000);
                } else if (valueDistanceFormat > 2 && valueDistanceFormat <= 10) {
                    double price = valueDistanceFormat * Double.parseDouble(Common.currentDriver.getThreeToTen());
                    return (int) (price / 1000);
                } else if (valueDistanceFormat > 10 && valueDistanceFormat <= 20) {
                    double price = valueDistanceFormat * Double.parseDouble(Common.currentDriver.getElevenToTwenty());
                    return (int) (price / 1000);
                } else if (valueDistanceFormat > 20) {
                    double price = valueDistanceFormat * Double.parseDouble(Common.currentDriver.getBiggerTwenty());
                    return (int) (price / 1000);
                }
            }
        }
        return 0;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyFirebaseMessaging.MESSAGE_KEY);
            if (MyFirebaseMessaging.CANCEL_TRIP_TITLE.equals(message)) {
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        mTrackingMap.clear();
    }
}
