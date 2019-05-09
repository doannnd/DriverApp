package com.nguyendinhdoan.driverapp.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
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
import com.nguyendinhdoan.driverapp.model.Token;
import com.nguyendinhdoan.driverapp.remote.IGoogleAPI;
import com.nguyendinhdoan.driverapp.services.MyFirebaseIdServices;
import com.nguyendinhdoan.driverapp.utils.CommonUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivity extends AppCompatActivity
        implements OnMapReadyCallback, CompoundButton.OnCheckedChangeListener,
        View.OnTouchListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String TAG = "DRIVER_ACTIVITY";
    public static final String DRIVER_LOCATION_TABLE_NAME = "driver_location";

    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    public static final float DRIVER_MAP_ZOOM = 15.0F;

    public static final String DIRECTION_ROUTES_KEY = "routes";
    public static final String DIRECTION_POLYLINE_KEY = "overview_polyline";
    public static final String DIRECTION_POINT_KEY = "points";
    public static final int DIRECTION_PADDING = 150;
    private static final float POLYLINE_WIDTH = 5F;
    private static final long DIRECTION_ANIMATE_DURATION = 3000L;
    private static final long DRAW_PATH_TIME_OUT = 3000L;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 9000;
    private static final String DRIVER_TABLE_NAME = "drivers";
    private static final String NV_CODE = "VN";
    private static final double DISTANCE_RESTRICT = 100000;
    private static final double HEADING_NORTH = 0;
    private static final double HEADING_SOUTH = 180;
    private static final int UPLOAD_REQUEST_CODE = 10;
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";
    private static final String PHONE_KEY = "phone";
    private static final String AVATAR_URL_KEY = "avatarUrl";

    private SwitchCompat stateDriverSwitch;
    private EditText destinationEditText;
    private ProgressBar driverProgressBar;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView uploadImageView;
    private TextInputEditText emailEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    //private TextInputLayout layoutName, layoutPhone, layoutEmail
    private Button stopDirectionButton;
    private FloatingActionButton startDirectionButton;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseAuth driverAuth;
    private GoogleMap driverMap;
    private GeoFire driverGeoFire;
    private Marker driverMarker;

    private LatLng startPosition;
    private LatLng endPosition;
    private IGoogleAPI mServices;
    private List<LatLng> directionPolylineList;
    private Polyline grayPolyline;
    private Polyline blackPolyline;
    private Handler handler;
    private Marker carMarker;

    private AlertDialog loading;
    private StorageReference storageReference;
    private DatabaseReference driverTable;
    private LatLng destinationLocation;
    private String destination;
    private ValueAnimator polyLineAnimator;

    private int index = -1;
    private int next = 1;

    public static Intent start(Context context) {
        return new Intent(context, DriverActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        initViews();
        setupUI();
        addEvents();
        //autoCompletePlaces();
    }

    private void autoCompletePlaces() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // restrict places only in city
        LatLng pinLocation = new LatLng(Common.currentLocation.getLatitude(),
                Common.currentLocation.getLongitude());
        /*
         * distance: meter unit: 100000 = 100 km
         * heading: 0 - north, 180-south
         * */
        LatLng northSide = SphericalUtil.computeOffset(pinLocation, DISTANCE_RESTRICT, HEADING_NORTH);
        LatLng southSide = SphericalUtil.computeOffset(pinLocation, DISTANCE_RESTRICT, HEADING_SOUTH);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .setTypeFilter(TypeFilter.ADDRESS)
                .setCountry(NV_CODE)
                .setLocationBias(RectangularBounds.newInstance(southSide, northSide))
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvents() {
        stateDriverSwitch.setOnCheckedChangeListener(this);
        destinationEditText.setOnTouchListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        stopDirectionButton.setOnClickListener(this);
        startDirectionButton.setOnClickListener(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.driver_toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        stateDriverSwitch = findViewById(R.id.state_driver_switch);
        destinationEditText = findViewById(R.id.destination_edit_text);
        driverProgressBar = findViewById(R.id.driver_progress_bar);
        stopDirectionButton = findViewById(R.id.stop_direction_button);
        startDirectionButton = findViewById(R.id.start_direction_button);
    }

    private void setupUI() {
        setupLoading();
        setupToolbar();
        setupNavigationView();
        setupGoogleMap();
        setupFirebase();
        setupStateDriver();
        setupLocation();
        setupRetrofit();
        setupPlacesAPI();
        init();
        updateTokenToDatabase();
    }

    private void setupLoading() {
        loading = new SpotsDialog.Builder()
                .setContext(this)
                .build();
    }

    private void setupNavigationView() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        updateInforDriver();
    }

    private void updateInforDriver() {
        View headerView = navigationView.getHeaderView(0);
        final TextView nameTextView = headerView.findViewById(R.id.name_text_view);
        final TextView emailTextView = headerView.findViewById(R.id.email_text_view);
        final TextView starTextView = headerView.findViewById(R.id.star_text_view);
        final CircleImageView avatarImageView = headerView.findViewById(R.id.avatar_image_view);

        FirebaseUser driver = FirebaseAuth.getInstance().getCurrentUser();
        if (driver != null) {
            // find driver with driver id
            String driverId = driver.getUid();
            driverTable = FirebaseDatabase.getInstance()
                    .getReference(DRIVER_TABLE_NAME).child(driverId);

            driverTable.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Common.currentDriver = dataSnapshot.getValue(Driver.class);

                    // display information of driver on navigation header.
                    if (Common.currentDriver != null) {
                        nameTextView.setText(Common.currentDriver.getName());
                        emailTextView.setText(Common.currentDriver.getEmail());
                        starTextView.setText(Common.currentDriver.getRates());
                        Glide.with(DriverActivity.this).load(Common.currentDriver.getAvatarUrl())
                                .placeholder(R.drawable.ic_profile)
                                .into(avatarImageView);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: error load profile driver" + databaseError);
                }
            });
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupStateDriver() {
        DatabaseReference onlineDriver = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        FirebaseUser driver = driverAuth.getCurrentUser();
        if (driver != null) {
            String driverId = driver.getUid();
            final DatabaseReference currentLocationDriver = FirebaseDatabase.getInstance()
                    .getReference(DRIVER_LOCATION_TABLE_NAME).child(driverId);

            // when driver offline <=> driver uncheck switch widget, we will remove location of driver in database
            // online driver has data change ==> online or offline
            onlineDriver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // DELETE if driver is disconnect
                    currentLocationDriver.onDisconnect().removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled: error in remove current location " +
                            "of driver when driver offline" + databaseError);
                }
            });
        }
    }

    private void updateTokenToDatabase() {
        final DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(MyFirebaseIdServices.TOKEN_TABLE_NAME);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Token token = new Token(newToken);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    tokenTable.child(userId).setValue(token)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "update token at [DriverActivity] success ");
                                    } else {
                                        Log.e(TAG, "update new token at [DriverActivity] failed ");
                                    }
                                }
                            });
                }
            }
        });
    }


    private void setupPlacesAPI() {
        Places.initialize(this, getString(R.string.google_api_key));
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

        storageReference = FirebaseStorage.getInstance().getReference();
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
        if (Common.currentLocation != null && stateDriverSwitch.isChecked()) {

            // get information save in driver_location table on firebase
            FirebaseUser user = driverAuth.getCurrentUser();
            if (user != null) {
                String driverId = user.getUid();
                final double driverLatitude = Common.currentLocation.getLatitude();
                final double driverLongitude = Common.currentLocation.getLongitude();

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
        // show title marker
        driverMarker.showInfoWindow();

        // move camera
        driverMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(driverLatitude, driverLongitude), DRIVER_MAP_ZOOM)
        );

        // hide progress bar complete display current location
        driverProgressBar.setVisibility(View.INVISIBLE);

        // check current location change
        if (directionPolylineList != null) {
            driverMap.clear();

            // add new marker
            // draw marker on google map
            driverMarker = driverMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                    .position(new LatLng(Common.currentLocation.getLatitude(), Common.currentLocation.getLongitude()))
                    .title(getString(R.string.title_of_you))
            );
            // show title marker
            driverMarker.showInfoWindow();

            // draw
            handleDriverDirection(destinationLocation);
        }

        if (polyLineAnimator != null) {
            polyLineAnimator.cancel();
        }
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

        setupMap();
    }

    private void setupMap() {
        driverMap.setTrafficEnabled(false);
        driverMap.setBuildingsEnabled(false);
        driverMap.setIndoorEnabled(false);
        driverMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // if switch is checked --> we set connect to firebase database
            FirebaseDatabase.getInstance().goOnline();

            showSnackBar(getString(R.string.you_are_online));
            driverProgressBar.setVisibility(View.GONE);
            startLocationUpdates();
        } else {

            // if switch isn't check --> we set disconnect to firebase database
            FirebaseDatabase.getInstance().goOffline();

            showSnackBar(getString(R.string.you_are_offline));
            stopLocationUpdates();
            // clear
            driverMap.clear();
            if (handler != null) {
                handler.removeCallbacks(drawPathRunnable);
            }
            // if marker exist --> delete
            if (driverMarker != null) {
                driverMarker.remove();
            }

            if (polyLineAnimator != null) {
                polyLineAnimator.cancel();
            }

            destinationEditText.setText("");
            directionPolylineList = null;
        }
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
       /* View view = snackbar.getView();
        view.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        TextView textSnack = view.findViewById(android.support.design.R.id.snackbar_text);
        textSnack.setTextColor(getResources().getColor(R.color.colorBlack));*/
        snackbar.show();
    }

    private void handleDriverDirection(LatLng destinationLocation) {

        //driverProgressBar.setVisibility(View.VISIBLE);

        if (directionPolylineList != null) {
            driverMap.clear();

            // add new marker
            // draw marker on google map
            driverMarker = driverMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
                    .position(new LatLng(Common.currentLocation.getLatitude(), Common.currentLocation.getLongitude()))
                    .title(getString(R.string.title_of_you))
            );
            // show title marker
            driverMarker.showInfoWindow();
        }

        if (polyLineAnimator != null) {
            polyLineAnimator.cancel();
        }

        // save current position
        double currentLatitude = Common.currentLocation.getLatitude();
        double currentLongitude = Common.currentLocation.getLongitude();
        LatLng currentPosition = new LatLng(currentLatitude, currentLongitude);

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
        driverMap.moveCamera(cameraUpdate);

        // handle information display of direction gray polyline
        PolylineOptions grayPolylineOptions = new PolylineOptions();
        grayPolylineOptions.color(Color.GRAY);
        grayPolylineOptions.width(POLYLINE_WIDTH);
        grayPolylineOptions.startCap(new SquareCap());
        grayPolylineOptions.endCap(new SquareCap());
        grayPolylineOptions.jointType(JointType.ROUND);
        grayPolylineOptions.addAll(directionPolylineList);

        // display black polyline overlay gray polyline on google map
        grayPolyline = driverMap.addPolyline(grayPolylineOptions);

        PolylineOptions blackPolylineOptions = new PolylineOptions();
        blackPolylineOptions.color(Color.BLACK);
        blackPolylineOptions.width(POLYLINE_WIDTH);
        blackPolylineOptions.startCap(new SquareCap());
        blackPolylineOptions.endCap(new SquareCap());
        blackPolylineOptions.jointType(JointType.ROUND);

        // display black polyline on map
        blackPolyline = driverMap.addPolyline(blackPolylineOptions);

        // display default marker at destination position
        int destinationPosition = directionPolylineList.size() - 1;
        Marker destinationMarker = driverMap.addMarker(new MarkerOptions()
                .position(directionPolylineList.get(destinationPosition))
                .title(destinationEditText.getText().toString())
        );
        // show destination marker title
        destinationMarker.showInfoWindow();

        animateDirectionPolyline();
    }

    private void animateDirectionPolyline() {
        // animation polyline
        polyLineAnimator = ValueAnimator.ofInt(0, 100);
        polyLineAnimator.setDuration(DIRECTION_ANIMATE_DURATION);
        polyLineAnimator.setInterpolator(new LinearInterpolator());
        polyLineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        polyLineAnimator.setRepeatMode(ValueAnimator.RESTART);
        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                List<LatLng> points = grayPolyline.getPoints();
                int percentValue = (int) valueAnimator.getAnimatedValue();
                int size = points.size();
                int newPoints = (int) (size * (percentValue / 100.0f));
                List<LatLng> p = points.subList(0, newPoints);
                blackPolyline.setPoints(p);
            }
        });
        polyLineAnimator.start();

        // hide loading
        //driverProgressBar.setVisibility(View.GONE);
        // add marker animate on direction polyline

        // show detail animate direction polyline
        //displayDetailDirectionPolyline();
    }

    private void displayDetailDirectionPolyline() {
        // add marker for direction detail
        carMarker = driverMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(Common.currentLocation.getLatitude(), Common.currentLocation.getLongitude()))
                        .flat(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
        );
        handler = new Handler();
        handler.postDelayed(drawPathRunnable, DRAW_PATH_TIME_OUT);
    }

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < directionPolylineList.size() - 1) {
                index++;
                next = index + 1;
            }

            if (index < directionPolylineList.size() - 1) {
                startPosition = directionPolylineList.get(index);
                endPosition = directionPolylineList.get(next);
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = animation.getAnimatedFraction();
                    double lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                    double lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));

                    driverMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(DRIVER_MAP_ZOOM)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude &&
                startPosition.longitude < endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (startPosition.latitude >= endPosition.latitude &&
                startPosition.longitude < endPosition.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        } else if (startPosition.latitude >= endPosition.latitude &&
                startPosition.longitude >= endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        } else if (startPosition.latitude < endPosition.latitude &&
                startPosition.longitude >= endPosition.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        }
        return -1;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (stateDriverSwitch.isChecked()) {
                    autoCompletePlaces();
                } else {
                    showSnackBar(getString(R.string.please_change_state_you));
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.performClick();
                break;
            }
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_trip_history: {
                break;
            }
            case R.id.nav_edit_profile: {
                if (stateDriverSwitch.isChecked()) {
                    showDialogUpdateProfile();
                } else {
                    showSnackBar(getString(R.string.please_change_state_you));
                }
                break;
            }
            case R.id.nav_sign_out: {
                signOut();
                break;
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogUpdateProfile() {
        AlertDialog.Builder editProfileDialog = new AlertDialog.Builder(this);
        editProfileDialog.setTitle(getString(R.string.edit_profile));

        View view = LayoutInflater.from(this).inflate(R.layout.edit_driver_profile, null);

        emailEditText = view.findViewById(R.id.email_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        phoneEditText = view.findViewById(R.id.phone_edit_text);
        uploadImageView = view.findViewById(R.id.upload_image_view);

       /* layoutEmail = view.findViewById(R.id.layout_email_profile);
        layoutName = view.findViewById(R.id.layout_name_profile);
        layoutPhone = view.findViewById(R.id.layout_phone_profile);*/

        // display information of driver ==> ui
        emailEditText.setText(Common.currentDriver.getEmail());
        nameEditText.setText(Common.currentDriver.getName());
        phoneEditText.setText(Common.currentDriver.getPhone());
        Glide.with(DriverActivity.this).load(Common.currentDriver.getAvatarUrl())
                .placeholder(R.drawable.ic_profile)
                .into(uploadImageView);

        // upload image from your phone
        uploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAvatarImage();
            }
        });

        editProfileDialog.setView(view);
        handelEditProfileDriver(editProfileDialog);
    }

    private void uploadAvatarImage() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent uploadIntent = new Intent();
                        uploadIntent.setAction(Intent.ACTION_GET_CONTENT);
                        uploadIntent.setType("image/*");
                        startActivityForResult(uploadIntent, UPLOAD_REQUEST_CODE);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            showSnackBar(getString(R.string.permission_denied));
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                if (stateDriverSwitch.isChecked()) { // if driver online --> working

                    Place place = Autocomplete.getPlaceFromIntent(data);
                    destination = place.getName();
                    destinationLocation = place.getLatLng();
                    Log.d(TAG, " place address: " + place.getAddress());
                    Log.d(TAG, "place name: " + place.getName());

                    if (destinationLocation != null) {
                        destinationEditText.setText(destination);
                        // handle direction with destination formatted
                        handleDriverDirection(destinationLocation);
                    }

                } else {
                    showSnackBar(getString(R.string.please_change_state_you));
                }

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(Objects.requireNonNull(data));
                Log.d(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                showSnackBar(getString(R.string.user_cancel_operation));
            }
        } else if (requestCode == UPLOAD_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {

                Uri imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                showSnackBar(getString(R.string.error_upload_image));
            }
        }

        // load avatar image wit crop image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                Uri resultUri = result.getUri();

                updateUIAndServer(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null) {
                Exception error = result.getError();
                Log.e(TAG, "onActivityResult: error upload image with crop" + error);
            }
        }
    }

    private void updateUIAndServer(final Uri resultUri) {
        loading.show();

        // random name image uploaded --> image code
        String imageName = UUID.randomUUID().toString();
        final StorageReference imageFolder = storageReference.child("images" + imageName);

        imageFolder.putFile(resultUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                // update uri to driver table
                                Map<String, Object> avatarUrl = new HashMap<>();
                                avatarUrl.put(AVATAR_URL_KEY, uri.toString());

                                // update avatar url to driver table
                                driverTable.updateChildren(avatarUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loading.dismiss();
                                        if (task.isSuccessful()) {
                                            // display avatar image
                                            uploadImageView.setImageURI(resultUri);

                                            showSnackBar(getString(R.string.upload_avatar_success));
                                            // update avatar navigation drawer
                                            updateInforDriver();
                                        } else {
                                            showSnackBar(getString(R.string.upload_avatar_failed));
                                        }
                                    }
                                });

                            }
                        });
                    }
                });
    }

    private void handelEditProfileDriver(AlertDialog.Builder editProfileDialog) {
        editProfileDialog.setPositiveButton(getString(R.string.edit_button_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loading.show();

                String name = Objects.requireNonNull(nameEditText.getText()).toString();
                String email = Objects.requireNonNull(emailEditText.getText()).toString();
                String phone = Objects.requireNonNull(phoneEditText.getText()).toString();

                Map<String, Object> driverInfor = new HashMap<>();

                if (CommonUtils.validateName(name)) {
                    driverInfor.put(NAME_KEY, name);
                }

                if (CommonUtils.validateEmail(email)) {
                    driverInfor.put(EMAIL_KEY, email);
                }

                if (CommonUtils.validatePhone(phone)) {
                    driverInfor.put(PHONE_KEY, phone);
                }

                driverTable.updateChildren(driverInfor).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loading.dismiss();

                        if (task.isSuccessful()) {
                            showSnackBar(getString(R.string.update_infor_success));
                            // update information in navigation drawer.
                            updateInforDriver();
                        } else {
                            showSnackBar(getString(R.string.update_infor_failed));
                        }
                    }
                });

            }
        }).setNegativeButton(getString(R.string.cancel_button_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close dialog and cancel update profile of driver.
                dialog.dismiss();
            }
        });

        // show edit profile dialog on ui
        editProfileDialog.show();
    }

    private void signOut() {
        // remove location if
        FirebaseUser driver = driverAuth.getCurrentUser();
        if (driver != null) {
            String driverId = driver.getUid();
            FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME).child(driverId).removeValue();
        }

        driverAuth.signOut();
        // jump to login activity
        Intent intentLogin = LoginActivity.start(this);
        intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentLogin);
        finish();
    }

    @Override
    protected void onDestroy() {
        FirebaseDatabase.getInstance().goOffline();

        stopLocationUpdates();
        // clear
        driverMap.clear();
        if (handler != null) {
            handler.removeCallbacks(drawPathRunnable);
        }
        // if marker exist --> delete
        if (driverMarker != null) {
            driverMarker.remove();
        }

        if (polyLineAnimator != null) {
            polyLineAnimator.cancel();
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_direction_button: {
                if (destination != null) {
                    displayDetailDirectionPolyline();
                    stopDirectionButton.setEnabled(true);
                }
                break;
            }
            case R.id.stop_direction_button: {

                driverMap.clear();

                if (handler != null) {

                    handler.removeCallbacks(drawPathRunnable);
                    // call display current location
                    //displayCurrentLocation();
                }

                destination = null;

                // if marker exist --> delete
                if (driverMarker != null) {
                    driverMarker.remove();
                }

                break;
            }
        }
    }
}
