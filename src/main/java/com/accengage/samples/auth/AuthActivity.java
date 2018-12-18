package com.accengage.samples.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.accengage.samples.R;
import com.accengage.samples.tracking.Trackers;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";
    private static final int RC_SIGN_IN = 123;

    private View mRootView;
    private Class<?> mActivityToStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.d("AuthActivity", "OK");

        mRootView = findViewById(R.id.root);

        String activityName = getIntent().getStringExtra("class_name");
        try {
            if (activityName != null)
                mActivityToStart = Class.forName(activityName);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "there is no goal activity");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = SignedInActivity.createIntent(this, null);
            if (mActivityToStart != null) {
                intent.putExtra("class_name", mActivityToStart.getName());
            }
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }
        showSnackbar(R.string.unknown_response);

    }

    public void signIn(View v) {
        Log.d("AuthActivity", "signin click");
        // Get an instance of AuthUI based on the default app
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), RC_SIGN_IN);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "sign-in token " + response.getIdpToken());
            if (mActivityToStart != null) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                AuthUtils.writeUser(currentUser);
                AuthUtils.sendUserInfo(new Trackers(this), currentUser);
                startActivity(new Intent(this, mActivityToStart));
            } else {
                startActivity(SignedInActivity.createIntent(this, response));
            }
            finish();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, AuthActivity.class);
        return in;
    }

    public static Intent createIntent(Context context, String activityClassName) {
        Log.d(TAG, "createIntent, activity to start after authentication: " + activityClassName);
        Intent in = createIntent(context)
                .putExtra("class_name", activityClassName);
        return in;
    }
}
