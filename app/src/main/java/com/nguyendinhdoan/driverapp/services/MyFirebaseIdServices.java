package com.nguyendinhdoan.driverapp.services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.nguyendinhdoan.driverapp.model.Token;

/**
 *
*  update token to database
* */
public class MyFirebaseIdServices extends FirebaseMessagingService {
    public static final String TAG = "TOKEN_SERVICE";
    public static final String TOKEN_TABLE_NAME = "tokens";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        updateTokenToDatabase(token);
    }

    /**
     * update new token to table [tokens] in database
     * */
    private void updateTokenToDatabase(String newToken) {
        DatabaseReference tokenTable = FirebaseDatabase.getInstance().getReference(TOKEN_TABLE_NAME);

        Token token = new Token(newToken);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // if user already login, we must update token
            String userId = user.getUid();
            tokenTable.child(userId).setValue(token)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "update token [SERVICES] to database success");
                            } else {
                                Log.e(TAG, "update token [SERVICES] failed");
                            }
                        }
                    });
        }
    }
}
