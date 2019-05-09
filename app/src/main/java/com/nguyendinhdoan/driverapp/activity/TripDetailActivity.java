package com.nguyendinhdoan.driverapp.activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;
import java.util.Locale;

public class TripDetailActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener, RatingDialogListener {

    public static final String TRIP_DETAIL_KEY = "TRIP_DETAIL_KEY";
    private TextView dateTextView, distanceTextView;
    private TextView timeTextView, baseFareTextView;
    private TextView feeTextView, estimatedPayoutTextView;
    private TextView fromTextView, toTextView;
    private ImageView backImageView;
    private ImageView starUserImageView;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        initGoogleMap();
        initViews();
        addEvents();
    }

    private void addEvents() {
        backImageView.setOnClickListener(this);
        starUserImageView.setOnClickListener(this);
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
        starUserImageView = findViewById(R.id.star_user_image_view);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_view: {
                launchDriverActivity();
                break;
            }
            case R.id.star_user_image_view: {
                showDialog();
                break;
            }
        }
    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(3)
                .setTitle("Rate for user")
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
                .setDefaultComment("Comment here")
                .setStarColor(R.color.starColor)
                .setNoteDescriptionTextColor(R.color.noteDescriptionTextColor)
                .setTitleTextColor(R.color.titleTextColor)
                .setDescriptionTextColor(R.color.contentTextColor)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.hintTextColor)
                .setCommentTextColor(R.color.commentTextColor)
                .setCommentBackgroundColor(R.color.colorCommentBackground)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(TripDetailActivity.this)
                .show();
    }

    private void launchDriverActivity() {
        Intent intent = DriverActivity.start(this);
        intent.putExtra(TRIP_DETAIL_KEY, "restart");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNegativeButtonClicked() {
        Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNeutralButtonClicked() {
        Toast.makeText(this, "later", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositiveButtonClicked(int i, String s) {
        Toast.makeText(this, "submit", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.clear();
    }
}
