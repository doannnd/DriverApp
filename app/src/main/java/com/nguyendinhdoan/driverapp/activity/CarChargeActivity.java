package com.nguyendinhdoan.driverapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.model.Driver;
import com.nguyendinhdoan.driverapp.utils.CommonUtils;

import java.util.Objects;


public class CarChargeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String DRIVER_TABLE_NAME = "drivers";

    private Toolbar toolbar;
    private TextInputEditText licensePlatesEditText;
    private TextInputEditText vehicleNameEditText;
    private TextInputEditText zeroToTwoEditText;
    private TextInputEditText threeToTenEditText;
    private TextInputEditText elevenToTwentyEditText;
    private TextInputEditText biggerTwentyEditText;
    private Button confirmButton;

    private DatabaseReference driverTable;
    private Driver driver;
    private String driverId;

    public static Intent start(Context context) {
        return new Intent(context, CarChargeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_charge);

        if (CommonUtils.isNetworkConnected(this)) {
            initViews();
            setupToolbar();
            initDatabase();
            addEvents();
        }
    }

    private void initDatabase() {
        if (getIntent() != null) {
            driver = getIntent().getParcelableExtra(VerifyPhoneActivity.DRIVER_KEY);
        }
        driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME);
    }

    private void addEvents() {
        confirmButton.setOnClickListener(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        licensePlatesEditText = findViewById(R.id.licence_plates_edit_text);
        vehicleNameEditText = findViewById(R.id.vehicle_name_edit_text);
        zeroToTwoEditText = findViewById(R.id.zero_to_two);
        threeToTenEditText = findViewById(R.id.three_to_ten);
        elevenToTwentyEditText = findViewById(R.id.eleven_to_twenty);
        biggerTwentyEditText = findViewById(R.id.bigger_twenty);
        confirmButton = findViewById(R.id.confirm_button);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_button) {
            updateInforDriver();
        }
    }

    private void updateInforDriver() {
       String licensePlates = Objects.requireNonNull(licensePlatesEditText.getText()).toString();
       String vehicleName = Objects.requireNonNull(vehicleNameEditText.getText()).toString();
       String zeroToTwo = Objects.requireNonNull(zeroToTwoEditText.getText()).toString();
       String threeToTen = Objects.requireNonNull(threeToTenEditText.getText()).toString();
       String elevenToTwenty = Objects.requireNonNull(elevenToTwentyEditText.getText()).toString();
       String biggerTwenty = Objects.requireNonNull(biggerTwentyEditText.getText()).toString();

        if (licensePlates.isEmpty() || vehicleName.isEmpty() ||
                zeroToTwo.isEmpty() || threeToTen.isEmpty() ||
                elevenToTwenty.isEmpty() || biggerTwenty.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    (R.string.field_empty_text), Snackbar.LENGTH_LONG ).show();
        } else {

            driverId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            Driver driverFull = new Driver(
                    driverId,
                    driver.getName(),
                    driver.getEmail(),
                    driver.getPhone(),
                    licensePlates,
                    vehicleName,
                    zeroToTwo,
                    threeToTen,
                    elevenToTwenty,
                    biggerTwenty
            );

            driverTable.child(driverId).setValue(driverFull).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        launchDriverActivity();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                Objects.requireNonNull(task.getException()).getMessage(),
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void launchDriverActivity() {
        Intent intent = DriverActivity.start(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
