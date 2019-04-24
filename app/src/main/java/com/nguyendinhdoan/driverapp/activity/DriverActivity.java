package com.nguyendinhdoan.driverapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.common.Common;
import com.nguyendinhdoan.driverapp.remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivity extends FragmentActivity
        implements OnMapReadyCallback,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final String TAG = "DRIVER_ACTIVITY";
    public static final String DRIVER_LOCATION_TABLE_NAME = "driver_location";

    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    public static final float DRIVER_MAP_ZOOM = 15.0F;

    public static final String DIRECTION_ROUTES_KEY = "routes";
    public static final String DIRECTION_POLYLINE_KEY = "overview_polyline";
    public static final String DIRECTION_POINT_KEY = "points";
    public static final int DIRECTION_PADDING = 100;
    private static final float POLYLINE_WIDTH = 5F;

    private Switch stateDriverSwitch;
    private EditText destinationEditText;
    private Button goButton;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseAuth driverAuth;
    private GoogleMap driverMap;
    private Location lastLocation;
    private GeoFire driverGeoFire;
    private Marker driverMarker;

    private LatLng currentPosition;
    private IGoogleAPI mServices;
    private List<LatLng> directionPolylineList;
    private Polyline grayPolyline;
    private Polyline blackPolyline;

    public static Intent start(Context context) {
        return new Intent(context, DriverActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        initViews();
        setupUI();
        addEvents();
    }

    private void addEvents() {
        stateDriverSwitch.setOnCheckedChangeListener(this);
        goButton.setOnClickListener(this);
    }

    private void initViews() {
        stateDriverSwitch = findViewById(R.id.state_driver_switch);
        destinationEditText = findViewById(R.id.destination_edit_text);
        goButton = findViewById(R.id.go_button);
    }

    private void setupUI() {
        setupGoogleMap();
        setupLocation();
        setupFirebase();
        setupRetrofit();
        init();
    }

    private void init() {
        directionPolylineList = new ArrayList<>();
    }

    private void setupRetrofit() {
        mServices = Common.getGoogleAPI();
    }

    private void setupFirebase() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME);
        driverGeoFire = new GeoFire(driverLocation);
        driverAuth = FirebaseAuth.getInstance();
    }

    private void setupGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driver_map);
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
                            if (ActivityCompat.checkSelfPermission(DriverActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(DriverActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest, locationCallback, Looper.myLooper());
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // TODO: ....
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
                lastLocation = locationResult.getLastLocation();
                Log.d(TAG, "current location latitude: " + lastLocation.getLatitude());
                Log.d(TAG, "current location longitude: " + lastLocation.getLongitude());

                // display current location on the google map
                displayCurrentLocation();
            }
        };

    }

    private void displayCurrentLocation() {
        if (lastLocation != null && stateDriverSwitch.isChecked()) {

            // get information save in driver_location table on firebase
            FirebaseUser user = driverAuth.getCurrentUser();
            if (user != null) {
                String driverId = user.getUid();
                final double driverLatitude = lastLocation.getLatitude();
                final double driverLongitude = lastLocation.getLongitude();

                // save location of driver in realtime database and update location on google map
                driverGeoFire.setLocation(driverId, new GeoLocation(driverLatitude, driverLongitude),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error == null) {
                                    Log.d(TAG, "save current location of driver success");
                                    updateUI(driverLatitude, driverLongitude);
                                } else {
                                    Log.e(TAG, "have error in display current location: " + error);
                                }
                            }
                        });
            }
        }
    }

    private void updateUI(double driverLatitude, double driverLongitude) {

        if (driverMarker != null) {
            driverMarker.remove(); // if marker existed --> delete
        }

        // draw marker on google map
        driverMarker = driverMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                .position(new LatLng(driverLatitude, driverLongitude))
                .title(getString(R.string.title_of_you))
        );

        // move camera
        driverMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(driverLatitude, driverLongitude), DRIVER_MAP_ZOOM)
        );
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

    private void setupLocation() {
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        driverMap = googleMap;

        driverMap.setTrafficEnabled(false);
        driverMap.setBuildingsEnabled(false);
        driverMap.setIndoorEnabled(false);
        driverMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        driverMap.getUiSettings().setZoomControlsEnabled(true);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            showSnackBar(getString(R.string.you_are_online));
            startLocationUpdates();
        } else {
            showSnackBar(getString(R.string.you_are_offline));
            stopLocationUpdates();
            // if marker exist --> delete
            if (driverMarker != null) {
                driverMarker.remove();
            }
        }
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
    public void onClick(View v) {
        if (stateDriverSwitch.isChecked()) {
            if (v.getId() == R.id.go_button) {
                String destination = destinationEditText.getText().toString();
                String destinationFormatted = destination.replace(" ", "+");
                Log.d(TAG, "destination formatted: " + destinationFormatted);

                handleDriverDirection(destinationFormatted);
            }
        }
    }

    private void handleDriverDirection(String destinationFormatted) {
        // save current position
        double currentLatitude = lastLocation.getLatitude();
        double currentLongitude = lastLocation.getLongitude();
        currentPosition = new LatLng(currentLatitude, currentLongitude);

        try {
            //building direction url for driver
            String directionURL = Common.directionURL(currentPosition, destinationFormatted);
            Log.d(TAG, "direction url: " + directionURL);

            // show direction
            mServices.getDirectionPath(directionURL)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull  Call<String> call, @NonNull Response<String> response) {
                            handleDirectionJSON(response.body());
                        }

                        @Override
                        public void onFailure(@NonNull  Call<String> call,@NonNull Throwable t) {
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
        driverMap.moveCamera(cameraUpdate);

        // handle information display of direction gray polyline
        PolylineOptions grayPolylineOptions = new PolylineOptions();
        grayPolylineOptions.color(Color.GRAY);
        grayPolylineOptions.width(POLYLINE_WIDTH);
        grayPolylineOptions.startCap(new SquareCap());
        grayPolylineOptions.endCap(new SquareCap());
        grayPolylineOptions.jointType(JointType.ROUND);
        grayPolylineOptions.addAll(directionPolylineList);

        // display on google map
        grayPolyline = driverMap.addPolyline(grayPolylineOptions);
    }


}
