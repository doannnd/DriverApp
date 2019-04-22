package com.nguyendinhdoan.driverapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class VerifyPhoneActivity extends AppCompatActivity {

    private Toolbar toolbar;

    public static Intent start(Context context) {
        return new Intent(context, VerifyPhoneActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        initViews();
        setupUI();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupUI() {
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backLoginScreen();
    }

    private void backLoginScreen() {
        Intent intentLogin = LoginActivity.start(this);
        startActivity(intentLogin);
        finish();
    }
}
