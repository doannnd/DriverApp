package com.nguyendinhdoan.driverapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.nguyendinhdoan.driverapp.services.MyFirebaseIdServices;
import com.nguyendinhdoan.driverapp.utils.CommonUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class DriverActivity extends AppCompatActivity
        implements OnMapReadyCallback, CompoundButton.OnCheckedChangeListener
        , NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "DRIVER_ACTIVITY";
    public static final String DRIVER_LOCATION_TABLE_NAME = "driver_location";

    public static final long LOCATION_REQUEST_INTERVAL = 5000L;
    public static final long LOCATION_REQUEST_FASTEST_INTERVAL = 3000L;
    public static final float LOCATION_REQUEST_DISPLACEMENT = 10.0F;
    public static final float DRIVER_MAP_ZOOM = 15.0F;

    private static final String DRIVER_TABLE_NAME = "drivers";
    private static final int UPLOAD_REQUEST_CODE = 10;
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";
    private static final String PHONE_KEY = "phone";
    private static final String AVATAR_URL_KEY = "avatarUrl";
    public static final String DRIVER_ID_KEY = "DRIVER_ID_KEY";
    public static final String STATE_KEY = "state";

    private SwitchCompat stateDriverSwitch;
    private ProgressBar driverProgressBar;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView uploadImageView;
    private TextInputEditText emailEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseAuth driverAuth;
    private GoogleMap driverMap;
    private GeoFire driverGeoFire;
    private Marker driverMarker;

    private AlertDialog loading;
    private StorageReference storageReference;
    private DatabaseReference driverTable;



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

        restartScreen();
        updateStateDriverNotWorking();
    }

    private void restartScreen() {
        if (getIntent() != null) {
            String message = getIntent().getStringExtra(EndGameActivity.DRIVER_RESTART_KEY);
            if (message != null) {
                stateDriverSwitch.setChecked(true);
                FirebaseDatabase.getInstance().goOnline();
            }
        }
    }

    private void updateStateDriverNotWorking() {
        Map<String, Object> driverUpdateState = new HashMap<>();
        driverUpdateState.put(STATE_KEY, getString(R.string.state_not_working));

        DatabaseReference driverTable = FirebaseDatabase.getInstance().getReference(DriverActivity.DRIVER_TABLE_NAME);
        driverTable.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .updateChildren(driverUpdateState)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "UPDATE STATE SUCCESS");
                        } else {
                            Log.e(TAG, "UPDATE SATE FAILED");
                        }
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvents() {
        stateDriverSwitch.setOnCheckedChangeListener(this);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.driver_toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        stateDriverSwitch = findViewById(R.id.state_driver_switch);
        driverProgressBar = findViewById(R.id.driver_progress_bar);
    }

    private void setupUI() {
        setupLoading();
        setupToolbar();
        setupNavigationView();
        setupGoogleMap();
        setupFirebase();
        setupStateDriver();
        setupLocation();
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
            driverMarker.remove();
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
        driverProgressBar.setVisibility(View.VISIBLE);

    }


    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(LOCATION_REQUEST_DISPLACEMENT);
    }

    private void stopLocationUpdates() {
        if (stateDriverSwitch.isChecked()) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
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
            // if marker exist --> delete
            if (driverMarker != null) {
                driverMarker.remove();
            }
        }
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.show();
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
                if (stateDriverSwitch.isChecked()) {
                    launchHistoryActivity();
                } else {
                    showSnackBar(getString(R.string.please_change_state_you));
                }
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

    private void launchHistoryActivity() {
        FirebaseUser driver = FirebaseAuth.getInstance().getCurrentUser();
        if (driver != null) {
            String driverId = driver.getUid();
            Intent intentHistory = HistoryActivity.start(this);
            intentHistory.putExtra(DRIVER_ID_KEY, driverId);

            intentHistory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHistory);
            //finish();
        }
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

        if (requestCode == UPLOAD_REQUEST_CODE) {
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
        // remove location
        FirebaseUser driver = driverAuth.getCurrentUser();
        if (driver != null) {
            String driverId = driver.getUid();
            FirebaseDatabase.getInstance().getReference(DRIVER_LOCATION_TABLE_NAME).child(driverId).removeValue();
        }

        driverAuth.signOut();

        Intent intentLogin = LoginActivity.start(this);
        intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentLogin);
        finish();
    }

    @Override
    protected void onDestroy() {
        FirebaseDatabase.getInstance().goOffline();
        stopLocationUpdates();
        driverMap.clear();
        if (driverMarker != null) {
            driverMarker.remove();
        }
        super.onDestroy();
    }

}
