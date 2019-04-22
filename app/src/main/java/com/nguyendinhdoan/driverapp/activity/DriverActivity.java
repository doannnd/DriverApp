package com.nguyendinhdoan.driverapp.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nguyendinhdoan.driverapp.R;

public class DriverActivity extends AppCompatActivity {

    public static Intent start(Context context) {
        return new Intent(context, DriverActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
    }
}
