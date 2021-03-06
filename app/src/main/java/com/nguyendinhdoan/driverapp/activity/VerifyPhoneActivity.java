package com.nguyendinhdoan.driverapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.model.Driver;
import com.nguyendinhdoan.driverapp.utils.CommonUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity implements
        View.OnClickListener, TextView.OnEditorActionListener {

    public static final String TAG = "PHONE_ACTIVITY";
    public static final long INITIAL_COUNT_DOWN = 60000L; // 60s
    public static final long COUNT_DOWN_INTERVAL = 1000L; // 1s
    public static final long VERIFICATION_CODE_TIME_OUT = 60L; // 60s
    public static final String DRIVER_KEY = "DRIVER_KEY";


    private ConstraintLayout rootLayout;
    private Toolbar toolbar;
    private TextView timeOutTextView;
    private TextView phoneTextView;
    private EditText codeEditText;
    private Button verificationCodeButton;
    private ProgressBar progressBar;


    private CountDownTimer countDownTimer;
    private String verificationId;
    private Driver driver;

    public static Intent start(Context context) {
        return new Intent(context, VerifyPhoneActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        if (CommonUtils.isNetworkConnected(this)) {
            FirebaseDatabase.getInstance().goOnline();
            initViews();
            setupUI();
            addEvents();
        }
    }

    private void initViews() {
        rootLayout = findViewById(R.id.root_layout);
        toolbar = findViewById(R.id.toolbar);
        timeOutTextView = findViewById(R.id.time_out_text_view);
        phoneTextView = findViewById(R.id.phone_text_view);
        codeEditText = findViewById(R.id.code_edit_text);
        verificationCodeButton = findViewById(R.id.verification_code_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupUI() {
        setupToolbar();
        setupTimeOut();
        displayPhoneNumber();
        sendVerificationCode();
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

    private void displayPhoneNumber() {
        driver = getIntent().getParcelableExtra(LoginActivity.DRIVER_KEY);
        Log.d(TAG, "phone: " + driver.getPhone());

        // set phone number for text view
        phoneTextView.setText(getString(R.string.phone_number, driver.getPhone()));
    }

    private void sendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84" + driver.getPhone(),
                VERIFICATION_CODE_TIME_OUT,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        verificationId = s;
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.d(TAG, "error: " + e.getMessage());
                        showSnackBar(e.getMessage());
                    }
                }
        );
    }

    private void addEvents() {
        codeEditText.setOnEditorActionListener(this);
        verificationCodeButton.setOnClickListener(this);
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

    private void showSnackBar(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.verification_code_button) {
            CommonUtils.hideKeyboard(this);
            verifyCodeAndLoginWithCredential();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            CommonUtils.hideKeyboard(this);
            verifyCodeAndLoginWithCredential();
            return true;
        }
        return false;
    }

    private void verifyCodeAndLoginWithCredential() {
        String code = codeEditText.getText().toString();
        if (TextUtils.isEmpty(code) || code.length() < 6) {
            showSnackBar(getString(R.string.error_code));
            codeEditText.requestFocus();
            return;
        }

        // login
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        loginWithCredential(credential, driver);
    }

    private void loginWithCredential(PhoneAuthCredential credential, final Driver driver) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            launchCarChargeScreen(driver);
                        } else {
                            Log.e(TAG, "error: " + task.getException());
                            showSnackBar(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    private void launchCarChargeScreen(Driver driver) {
        Intent intentDriver = CarChargeActivity.start(this);
        intentDriver.putExtra(DRIVER_KEY, driver);
        intentDriver.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentDriver);
        finish();
    }

}
