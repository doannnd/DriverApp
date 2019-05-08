package com.nguyendinhdoan.driverapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.common.Common;
import com.nguyendinhdoan.driverapp.model.Notification;
import com.nguyendinhdoan.driverapp.model.Result;
import com.nguyendinhdoan.driverapp.model.Sender;
import com.nguyendinhdoan.driverapp.model.Token;
import com.nguyendinhdoan.driverapp.remote.IFirebaseMessagingAPI;
import com.nguyendinhdoan.driverapp.remote.IGoogleAPI;
import com.nguyendinhdoan.driverapp.services.MyFirebaseIdServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

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

    private static final String LOCATION_ADDRESS = "LOCATION_ADDRESS_KEY";
    private static final String DESTINATION_ADDRESS = "DESTINATION_ADDRESS_KEY";
    private static final String DIRECTION_LEGS_KEY = "legs";
    private static final String DIRECTION_DURATION_KEY = "duration";
    private static final String DIRECTION_DISTANCE_KEY = "distance";
    private static final String DIRECTION_ADDRESS_KEY = "end_address";
    private static final String DIRECTION_TEXT_KEY = "text";
    private static final String START_ADDRESS_KEY = "start_address";


    public static final String START_ADDRESS_INTENT_KEY = "START_ADDRESS_INTENT_KEY";
    public static final String END_ADDRESS_INTENT_KEY = "END_ADDRESS_INTENT_KEY";
    public static final String TIME_INTENT_KEY = "TIME_INTENT_KEY";
    public static final String DISTANCE_INTENT_KEY = "DISTANCE_INTENT_KEY";
    public static final String TOTAL_INTENT_KEY = "TOTAL_INTENT_KEY";
    public static final String LOCATION_START_INTENT_KEY = "LOCATION_START_INTENT_KEY";
    public static final String LOCATION_END_INTENT_KEY = "LOCATION_END_INTENT_KEY";


    private ProgressBar loadingProgressBar;
    private GoogleMap mTrackingMap;
    private Button startTripButton;
    private Toolbar trackingToolbar;
    private EditText userDestinationEditText;
    private TextView directionTextView;

    private double latitudeUser;
    private double longitudeUser;
    private String userId;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker driverMarker;
    private Polyline trackingPolyline;

    private IGoogleAPI mServices;
    private IFirebaseMessagingAPI mFirebaseService;
    private List<LatLng> directionPolylineList;

    private String destinationAddress;

    private Location pickupLocation;
    private double distanceUserAndDriver;
    private String unitDistance;
    private LatLng destinationUserTracking = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        initViews();
        initGoogleMap();
        setupUI();
        addEvents();
    }

    private void addEvents() {
        startTripButton.setOnClickListener(this);
        directionTextView.setOnClickListener(this);
    }

    private void initViews() {
        trackingToolbar = findViewById(R.id.tracking_toolbar);
        setupToolbar();

        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        startTripButton = findViewById(R.id.start_trip_button);
        directionTextView = findViewById(R.id.direction_text_view);
        userDestinationEditText = findViewById(R.id.user_destination_edit_text);
    }

    private void setupToolbar() {
        setSupportActionBar(trackingToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupUI() {
        if (getIntent() != null) {
            latitudeUser = getIntent().getDoubleExtra(UserCallActivity.LAT_USER, -1);
            longitudeUser = getIntent().getDoubleExtra(UserCallActivity.LNG_USER, -1);
            userId = getIntent().getStringExtra(UserCallActivity.ID_USER);
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

            if (driverMarker != null) {
                driverMarker.remove(); // if marker existed --> delete
            }

            // draw marker on google map
            driverMarker = mTrackingMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .position(new LatLng(driverLatitude, driverLongitude))
                    .title(getString(R.string.title_of_you))
            );

            driverMarker.showInfoWindow();

            // move camera
            mTrackingMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(new LatLng(driverLatitude, driverLongitude), DRIVER_MAP_ZOOM)
            );

            loadingProgressBar.setVisibility(View.GONE);

            if (trackingPolyline != null) {
                trackingPolyline.remove();
            }

            handleDriverDirection();
        }
    }

    private void handleDriverDirection() {
        // save current position
        double currentLatitude = Common.currentLocation.getLatitude();
        double currentLongitude = Common.currentLocation.getLongitude();
        LatLng currentPosition = new LatLng(currentLatitude, currentLongitude);
        LatLng destinationLocation = new LatLng(latitudeUser, longitudeUser);

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
            destinationAddress = legObject.getString(DIRECTION_ADDRESS_KEY);
            // display address user on edit text

            JSONObject distance = legObject.getJSONObject(DIRECTION_DISTANCE_KEY);
            String km = distance.getString(DIRECTION_TEXT_KEY);
            Log.d(TAG, "km: " + distance.getString(DIRECTION_TEXT_KEY));

            distanceUserAndDriver = Double.parseDouble(km.replaceAll("[^0-9\\\\.]", ""));
            String[] units = km.split(" ");
            unitDistance = units[1];
            Log.d(TAG, "distance user and driver: " + distanceUserAndDriver);
            Log.d(TAG, "unit distance: " + unitDistance);

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

        // display default marker at destination position
       /* int destinationPosition = directionPolylineList.size() - 1;
        mTrackingMap.addMarker(new MarkerOptions()
                .position(directionPolylineList.get(destinationPosition))
                .title("user")
        );*/
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
       /* View view = snackbar.getView();
        view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        TextView textSnack = view.findViewById(android.support.design.R.id.snackbar_text);
        textSnack.setTextColor(getResources().getColor(R.color.colorBlack));*/
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mTrackingMap = googleMap;

        // show marker of user: destination driver go
        mTrackingMap.addCircle(new CircleOptions()
                .center(new LatLng(latitudeUser, longitudeUser))
                .radius(CIRCLE_RADIUS)
                .strokeColor(Color.BLUE)
                .strokeWidth(CIRCLE_STROKE_WIDTH)
                .fillColor(CIRCLE_FILL_COLOR)
        );

      /*  Marker customerMaker = mTrackingMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitudeUser, longitudeUser))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                .title("customer")
        );
        customerMaker.showInfoWindow();*/

        handleGeoFencing();
    }

    private void handleGeoFencing() {
        DatabaseReference driverLocationTable = FirebaseDatabase.getInstance().getReference(DriverActivity.DRIVER_LOCATION_TABLE_NAME);
        GeoFire trackingGeoFire = new GeoFire(driverLocationTable);

        GeoQuery trackingGeoQuery = trackingGeoFire.queryAtLocation(
                new GeoLocation(latitudeUser, longitudeUser),
                0.05 // 50m
        );

        trackingGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // if driver in radius = 50m --> notification for user ....
                sendNotificationArrivedToUser(userId);
                startTripButton.setEnabled(true);
                if (Common.userDestination != null && Common.destinationLocationUser != null) {
                    Toast.makeText(TrackingActivity.this, "true", Toast.LENGTH_SHORT).show();
                    userDestinationEditText.setText(Common.userDestination);
                    destinationUserTracking = Common.destinationLocationUser;
                } else {
                    userDestinationEditText.setText("");
                    Toast.makeText(TrackingActivity.this, "failed", Toast.LENGTH_SHORT).show();
                }
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

                            String bodyMessage = String.format("The driver %s has arrived at your location", Common.currentDriver.getName());
                            Notification notification = new Notification("Arrived", bodyMessage);
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

    private void sendDropOffNotification(final String userId) {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        tokenTable.orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Token token = postSnapshot.getValue(Token.class);

                            Notification notification = new Notification("DropOff", userId);
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
            if (startTripButton.getText().equals("START TRIP")) {
                pickupLocation = Common.currentLocation;
                startTripButton.setText(getString(R.string.drop_off_here));
            } else if (startTripButton.getText().equals("DROP OFF HERE")) {
                calculateCashFee(pickupLocation, Common.currentLocation);
            }
        } else if (v.getId() == R.id.direction_text_view) {
            if (startTripButton.isEnabled() && destinationUserTracking != null) {
                String uriDirectionWithGoogleMap = "google.navigation:q=" + destinationUserTracking.latitude + "," + destinationUserTracking.longitude;
                Uri gmmIntentUri = Uri.parse(uriDirectionWithGoogleMap);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } else {
                String uriDirectionWithGoogleMap = "google.navigation:q=" + latitudeUser + "," + longitudeUser;
                Uri gmmIntentUri = Uri.parse(uriDirectionWithGoogleMap);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
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
                                JSONObject routeObject = routes.getJSONObject(0);
                                JSONArray legs = routeObject.getJSONArray(DIRECTION_LEGS_KEY);
                                JSONObject legObject = legs.getJSONObject(0);

                                // get time and display on time text view
                                JSONObject time = legObject.getJSONObject(DIRECTION_DURATION_KEY);
                                String minutes = time.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "minutes: " + time.getString(DIRECTION_TEXT_KEY));

                                int timeFormatted = Integer.parseInt(minutes.replaceAll("\\D+", ""));

                                // get distance and display on distance text view
                                JSONObject distance = legObject.getJSONObject(DIRECTION_DISTANCE_KEY);
                                String km = distance.getString(DIRECTION_TEXT_KEY);
                                Log.d(TAG, "km: " + distance.getString(DIRECTION_TEXT_KEY));

                                double distanceFormatted = Double.parseDouble(km.replaceAll("[^0-9\\\\.]", ""));

                                /*String finalPrice = String.format(Locale.getDefault(), "%s km + %s minute = $%.2f", distanceFormatted, timeFormatted,
                                        Common.getPrice(distanceFormatted, timeFormatted));*/

                                // get end address and display on address text view
                                String destinationAddress = legObject.getString(DIRECTION_ADDRESS_KEY);
                                String locationAddress = legObject.getString(START_ADDRESS_KEY);
                                Log.d(TAG, "destination address: " + destinationAddress);
                                Log.d(TAG, "location address: " + locationAddress);

                                // send notification to user
                                sendDropOffNotification(userId);

                                Intent intentTripDetail = new Intent(TrackingActivity.this, TripDetailActivity.class);
                                intentTripDetail.putExtra(START_ADDRESS_INTENT_KEY, locationAddress);
                                intentTripDetail.putExtra(END_ADDRESS_INTENT_KEY, destinationAddress);
                                intentTripDetail.putExtra(TIME_INTENT_KEY, String.valueOf(timeFormatted));
                                intentTripDetail.putExtra(DISTANCE_INTENT_KEY, String.valueOf(distanceFormatted));
                                intentTripDetail.putExtra(TOTAL_INTENT_KEY, Common.getPrice(distanceFormatted, timeFormatted));
                                intentTripDetail.putExtra(LOCATION_START_INTENT_KEY,
                                        String.format(Locale.getDefault(), "%f,%f",
                                                pickupLocation.getLatitude(), pickupLocation.getLongitude())
                                );
                                intentTripDetail.putExtra(LOCATION_END_INTENT_KEY, Common.currentLocation.getLatitude() + "," + Common.currentLocation.getLongitude());

                                intentTripDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intentTripDetail);
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
}
