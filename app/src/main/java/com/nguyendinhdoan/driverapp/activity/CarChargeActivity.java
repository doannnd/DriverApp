package com.nguyendinhdoan.driverapp.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.model.Driver;

import java.util.HashMap;
import java.util.Map;


public class CarChargeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CarChargeActivity";
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

    public static Intent start(Context context) {
        return new Intent(context, CarChargeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_charge);

        initViews();
        setupToolbar();
        initDatabase();
        addEvents();
    }

    private void initDatabase() {
        driverTable = FirebaseDatabase.getInstance().getReference(DRIVER_TABLE_NAME);
    }

    private void addEvents() {
        confirmButton.setOnClickListener(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
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
       String licensePlates = licensePlatesEditText.getText().toString();
       String vehicleName = vehicleNameEditText.getText().toString();
       String zeroToTwo = zeroToTwoEditText.getText().toString();
       String threeToTen = threeToTenEditText.getText().toString();
       String elevenToTwenty = elevenToTwentyEditText.getText().toString();
       String biggerTwenty = biggerTwentyEditText.getText().toString();

        if (licensePlates.isEmpty() || vehicleName.isEmpty() ||
                zeroToTwo.isEmpty() || threeToTen.isEmpty() ||
                elevenToTwenty.isEmpty() || biggerTwenty.isEmpty()) {

            Map<String, Object> driverUpdate = new HashMap<>();
            driverUpdate.put("licensePlates", licensePlates);
            driverUpdate.put("vehicleName", vehicleName);
            driverUpdate.put("zeroToTwo", zeroToTwo);
            driverUpdate.put("threeToTen", threeToTen);
            driverUpdate.put("elevenToTwenty", elevenToTwenty);
            driverUpdate.put("biggerTwenty", biggerTwenty);

            driverTable.updateChildren(driverUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        launchDriverActivity();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        } else {
            Snackbar.make(findViewById(android.R.id.content),"field emtpy", Snackbar.LENGTH_LONG ).show();
        }
    }

    private void launchDriverActivity() {
        Intent intent = DriverActivity.start(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
