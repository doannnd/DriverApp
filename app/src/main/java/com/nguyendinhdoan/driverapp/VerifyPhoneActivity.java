package com.nguyendinhdoan.driverapp;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class VerifyPhoneActivity extends AppCompatActivity {


    public static final long INITIAL_COUNT_DOWN = 60000L; // 60s
    public static final long COUNT_DOWN_INTERVAL = 1000L; // 1s

    private Toolbar toolbar;
    private TextView timeOutTextView;
    private TextView phoneTextView;
    private EditText codeEditText;
    private Button verificationCodeButton;

    private CountDownTimer countDownTimer;

    public static Intent start(Context context) {
        return new Intent(context, VerifyPhoneActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        initViews();
        setupUI();
        addEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        timeOutTextView = findViewById(R.id.time_out_text_view);
        phoneTextView = findViewById(R.id.phone_text_view);
        codeEditText = findViewById(R.id.code_edit_text);
        verificationCodeButton = findViewById(R.id.verification_code_button);
    }

    private void setupUI() {
        setupToolbar();
        setupTimeOut();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void setupTimeOut() {
        countDownTimer = new CountDownTimer(INITIAL_COUNT_DOWN, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long timeLeft = millisUntilFinished / 1000;
                timeOutTextView.setText(getString(R.string.time_out_text, String.valueOf(timeLeft)));
            }

            @Override
            public void onFinish() {
                timeOutTextView.setText(Html.fromHtml(getString(R.string.label_request_new_code)));
            }
        }.start();
    }

    private void addEvents() {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
