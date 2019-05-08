package com.nguyendinhdoan.driverapp.activity;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.common.Common;

import java.util.Locale;

public class TripDetailActivity extends FragmentActivity implements OnMapReadyCallback {

    private TextView dateTextView, distanceTextView;
    private TextView timeTextView, baseFareTextView;
    private TextView feeTextView, estimatedPayoutTextView;
    private TextView fromTextView, toTextView;
    private ImageView backImageView;
    private TextView starUserTextView;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        initGoogleMap();
        initViews();
    }

    private void setupUI() {
        dateTextView.setText(Common.currentDate());

        if (getIntent() != null) {
            feeTextView.setText(String.format(Locale.getDefault(), "$ %.2f", getIntent().getDoubleExtra(TrackingActivity.TOTAL_INTENT_KEY, 0.0)));
            estimatedPayoutTextView.setText(String.format(Locale.getDefault(), "$ %.2f", getIntent().getDoubleExtra(TrackingActivity.TOTAL_INTENT_KEY, 0.0)));
            baseFareTextView.setText(String.format(Locale.getDefault(), "$ %.2f", Common.BASE_FARE));
            timeTextView.setText(String.format(Locale.getDefault(), "%s minute", getIntent().getStringExtra(TrackingActivity.TIME_INTENT_KEY)));
            distanceTextView.setText(String.format(Locale.getDefault(), "%s km", getIntent().getStringExtra(TrackingActivity.DISTANCE_INTENT_KEY)));
            fromTextView.setText(getIntent().getStringExtra(TrackingActivity.START_ADDRESS_INTENT_KEY));
            toTextView.setText(getIntent().getStringExtra(TrackingActivity.END_ADDRESS_INTENT_KEY));

            // add new marker current location fo driver
            String[] endLocation = getIntent().getStringExtra(TrackingActivity.LOCATION_END_INTENT_KEY).split(",");
            LatLng dropOffLocation = new LatLng(Double.parseDouble(endLocation[0]), Double.parseDouble(endLocation[1]));

            Marker marker = mMap.addMarker(
                    new MarkerOptions().position(dropOffLocation)
                            .title(getIntent().getStringExtra(TrackingActivity.END_ADDRESS_INTENT_KEY))
                            .icon(BitmapDescriptorFactory.defaultMarker())
            );
            marker.showInfoWindow();

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOffLocation, 12.0f));
        }

    }

    private void initViews() {
        dateTextView = findViewById(R.id.date_text_view);
        distanceTextView = findViewById(R.id.distance_text_view);
        timeTextView = findViewById(R.id.time_text_view);
        baseFareTextView = findViewById(R.id.base_fare_text_view);
        feeTextView = findViewById(R.id.fee_text_view);
        estimatedPayoutTextView = findViewById(R.id.estimated_payout_text_view);
        fromTextView = findViewById(R.id.from_text_view);
        toTextView = findViewById(R.id.to_text_view);
        backImageView = findViewById(R.id.back_image_view);
        starUserTextView = findViewById(R.id.star_user_text_view);
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

        setupUI();
        setupMap();
    }

    private void setupMap() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
