package com.nguyendinhdoan.driverapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.common.Common;

public class EndGameActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener {

    private Toolbar endGameToolbar;
    private ImageView closeImageView;
    private TextView dateTimeTextView;
    private TextView tripPriceTextView;

    private GoogleMap mMap;
    private String tripPrice;
    private  LatLng destinationLocation;
    private String endAddress;

    public static Intent start(Context context) {
        return new Intent(context, EndGameActivity.class);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);
        FirebaseDatabase.getInstance().goOnline();


        initViews();
        setupUI();
        addEvents();
    }

    private void addEvents() {
        closeImageView.setOnClickListener(this);
    }

    private void setupUI() {
        setupToolbar();
        initGoogleMap();
        displayInforUI();
    }

    private void displayInforUI() {
        // load date time
        dateTimeTextView.setText(Common.getCurrentDate());

        if (getIntent() != null) {
            tripPrice = getIntent().getStringExtra(TrackingActivity.TRIP_PRICE_INTENT_KEY);
            destinationLocation = getIntent().getParcelableExtra(TrackingActivity.LOCATION_END_INTENT_KEY);
            endAddress = getIntent().getStringExtra(TrackingActivity.END_ADDRESS_INTENT_KEY);
        }

        if (tripPrice != null) {
            tripPriceTextView.setText(getString(R.string.trip_price_text, tripPrice));
        }

        if (destinationLocation != null && endAddress != null) {
            mMap.addMarker(
                    new MarkerOptions().position(destinationLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .title(endAddress)
            );
        }
    }

    private void setupToolbar() {
        setSupportActionBar(endGameToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initViews() {
        endGameToolbar = findViewById(R.id.end_game_toolbar);
        closeImageView = findViewById(R.id.close_image_view);
        dateTimeTextView = findViewById(R.id.date_time_text_view);
        tripPriceTextView = findViewById(R.id.trip_price_text_view);
    }

    private void initGoogleMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trip_detail_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_image_view) {
            Intent intentUser = DriverActivity.start(this);
            intentUser.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentUser);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.clear();
    }
}
