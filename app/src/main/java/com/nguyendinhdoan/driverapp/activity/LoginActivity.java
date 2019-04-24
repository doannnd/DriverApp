package com.nguyendinhdoan.driverapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nguyendinhdoan.driverapp.R;
import com.nguyendinhdoan.driverapp.model.Driver;
import com.nguyendinhdoan.driverapp.utils.CommonUtils;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    public static final String DRIVER_KEY = "DRIVER_KEY";

    private TextInputLayout layoutName, layoutPhone, layoutEmail;
    private TextInputEditText nameEditText, phoneEditText, emailEditText;
    private Button loginButton;

    public static Intent start(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        addEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        onLoggedIn();
    }

    private void onLoggedIn() {
        FirebaseUser driver = FirebaseAuth.getInstance().getCurrentUser();
        if (driver != null) {
            launchDriverScreen();
        }
    }

    private void initViews() {
        layoutName = findViewById(R.id.layout_name);
        layoutEmail = findViewById(R.id.layout_email);
        layoutPhone = findViewById(R.id.layout_phone);
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        loginButton = findViewById(R.id.login_button);
    }

    private void addEvents() {
        loginButton.setOnClickListener(this);
        phoneEditText.setOnEditorActionListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button) {
            performLogin();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            CommonUtils.hideKeyboard(this);
            performLogin();
            return true;
        }
        return false;
    }

    private void performLogin() {

        String name = Objects.requireNonNull(nameEditText.getText()).toString();
        String email = Objects.requireNonNull(emailEditText.getText()).toString();
        String phone = Objects.requireNonNull(phoneEditText.getText()).toString();

        // validate name, email, phone
        if (!CommonUtils.validateName(name)) {
            layoutName.setError(getString(R.string.empty_name));
            layoutName.setErrorEnabled(true);
            return;
        } else {
            layoutName.setErrorEnabled(false);
        }

        if (!CommonUtils.validateEmail(email)) {
            layoutEmail.setError(getString(R.string.email_invalid));
            layoutEmail.setErrorEnabled(true);
            return;
        } else {
            layoutEmail.setErrorEnabled(false);
        }

        if (!CommonUtils.validatePhone(phone)) {
            layoutPhone.setError(getString(R.string.phone_invalid));
            layoutPhone.setErrorEnabled(true);
            return;
        } else {
            layoutPhone.setErrorEnabled(false);
        }

        // if fields valid jump verify phone screen
        launchVerifyPhoneScreen();
    }

    private void launchVerifyPhoneScreen() {
        Intent intentPhone = VerifyPhoneActivity.start(this);

        String name = Objects.requireNonNull(nameEditText.getText()).toString();
        String email = Objects.requireNonNull(emailEditText.getText()).toString();
        String phone = Objects.requireNonNull(phoneEditText.getText()).toString();
        Driver driver = new Driver(name, email, phone);
        intentPhone.putExtra(DRIVER_KEY, driver);

        startActivity(intentPhone);
        finish();
    }

    private void launchDriverScreen() {
        Intent intentDriver = DriverActivity.start(this);
        intentDriver.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentDriver);
        finish();
    }

}
