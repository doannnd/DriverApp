package com.nguyendinhdoan.driverapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.model.Circle;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "TrackingActivity";
    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    public static final float DRIVER_MAP_ZOOM = 15.0F;
    public static final String DIRECTION_ROUTES_KEY = "routes";
    public static final String DIRECTION_POLYLINE_KEY = "overview_polyline";
    public static final String DIRECTION_POINT_KEY = "points";
    public static final int DIRECTION_PADDING = 100;
    private static final float POLYLINE_WIDTH = 5F;
    private static final double CIRCLE_RADIUS = 50; // 50m
    private static final float CIRCLE_STROKE_WIDTH = 5.0F;
    private static final int CIRCLE_FILL_COLOR = 0x220000FF;


    private ProgressBar loadingProgressBar;

    private GoogleMap mTrackingMap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        initViews();
        initGoogleMap();
        setupUI();
    }

    private void initViews() {
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
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
                .findFragmentById(R.id.map);
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
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                    .position(new LatLng(driverLatitude, driverLongitude))
                    .title(getString(R.string.title_of_you))
            );

            // move camera
            mTrackingMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(new LatLng(driverLatitude, driverLongitude), DRIVER_MAP_ZOOM)
            );

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
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : directionPolylineList) {
            builder.include(latLng);
        }

        // handle display camera
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, DIRECTION_PADDING);
        mTrackingMap.moveCamera(cameraUpdate);

        // handle information display of direction gray polyline
        PolylineOptions grayPolylineOptions = new PolylineOptions();
        grayPolylineOptions.color(Color.GRAY);
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

        loadingProgressBar.setVisibility(View.GONE);
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
        View view = snackbar.getView();
        view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        TextView textSnack = view.findViewById(android.support.design.R.id.snackbar_text);
        textSnack.setTextColor(getResources().getColor(R.color.colorBlack));
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
}
