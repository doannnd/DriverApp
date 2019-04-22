package com.nguyendinhdoan.driverapp.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Pattern;

public class CommonUtils {

    public static final int HIDE_KEYBOARD_FLAGS = 0;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();

        if (view == null) {
            view = new View(activity);
        }

        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_KEYBOARD_FLAGS);
        }
    }

    public static boolean validateName(String name) {
        return name != null && !TextUtils.isEmpty(name);
    }

    public static boolean validateEmail(String email) {
        return email != null && !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean validatePhone(String phone) {
        return phone != null && !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isNetworkConnected(Activity activity) {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

}
