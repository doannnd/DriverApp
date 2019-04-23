package com.nguyendinhdoan.driverapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nguyendinhdoan.driverapp.R;

import java.util.List;

public class DriverActivity extends FragmentActivity
        implements OnMapReadyCallback,
        CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "DRIVER_ACTIVITY";
    public static final String DRIVER_LOCATION_TABLE_NAME = "driver_location";

    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    private static final float DRIVER_MAP_ZOOM = 15.0F;

    private Switch stateDriverSwitch;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseAuth driverAuth;
    private GoogleMap driverMap;
    private Location lastLocation;
    private GeoFire driverGeoFire;
    private Marker driverMarker;

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
    }

    private void initViews() {
        stateDriverSwitch = findViewById(R.id.state_driver_switch);
    }

    private void setupUI() {
        setupGoogleMap();
        setupLocation();
        setupFirebase();
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
}
