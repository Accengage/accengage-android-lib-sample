package com.accengage.samples.auth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accengage.samples.R;
import com.accengage.samples.base.BaseActivity;
import com.accengage.samples.tracking.Trackers;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.Iterator;
import java.util.List;


public class SignedInActivity extends BaseActivity {

    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";
    private static final String TAG = "SignedInActivity";

    private View mRootView;
    private ImageView mUserProfilePicture;
    private TextView mUserEmail;
    private TextView mUserDisplayName;
    private TextView mEnabledProviders;
    private Button mButtonVerifyEmail;
    private Button mButtonOk;

    private Class<?> mActivityToStart;
    private IdpResponse mIdpResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signed_in);

        mRootView = findViewById(android.R.id.content);
        mUserProfilePicture = (ImageView) findViewById(R.id.user_profile_picture);
        mUserEmail = (TextView) findViewById(R.id.user_email);
        mUserDisplayName = (TextView) findViewById(R.id.user_display_name);
        mEnabledProviders = (TextView) findViewById(R.id.user_enabled_providers);
        mButtonVerifyEmail = (Button) findViewById(R.id.btn_verify_email);
        mButtonOk = (Button) findViewById(R.id.btn_sign_ok);

        String activityName = getIntent().getStringExtra("class_name");
        try {
            if (activityName != null)
                mActivityToStart = Class.forName(activityName);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "there is no goal activity");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(AuthActivity.createIntent(this));
            finish();
            return;
        }

        if (currentUser.isEmailVerified()) {
            mButtonVerifyEmail.setEnabled(false);
            mButtonOk.setEnabled(true);
        } else {
            mButtonVerifyEmail.setEnabled(true);
            mButtonOk.setEnabled(false);
        }
        mIdpResponse = IdpResponse.fromResultIntent(getIntent());
        populateProfile();
        populateIdpToken();
        AuthUtils.writeUser(currentUser);
    }

    public void signOut(View v) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = (mActivityToStart != null) ?
                                    AuthActivity.createIntent(SignedInActivity.this, mActivityToStart.getName()) :
                                    AuthActivity.createIntent(SignedInActivity.this);
                            startActivity(intent);
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });
    }

    public void deleteAccountClicked(View v) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(AuthActivity.createIntent(SignedInActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.delete_account_failed);
                        }
                    }
                });
    }

    @MainThread
    private void populateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .fitCenter()
                    .into(mUserProfilePicture);
        }

        String name = TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : user.getDisplayName();
        String email = TextUtils.isEmpty(user.getEmail()) ? "No email" : user.getEmail();
        String verified = user.isEmailVerified() ? "verified" : "not verified";
        AuthUtils.sendUserInfo(new Trackers(this), user);
        mUserDisplayName.setText(name);
        mUserEmail.setText(email + " - " + verified);


        StringBuilder providerList = new StringBuilder(100);

        providerList.append("Providers used: ");

        List<? extends UserInfo> infos = user.getProviderData();
        if (infos == null || infos.isEmpty()) {
            providerList.append("none");
        } else {
            Iterator<? extends UserInfo> providerIter = infos.iterator();
            while (providerIter.hasNext()) {
                String provider = providerIter.next().getProviderId();
                if (GoogleAuthProvider.PROVIDER_ID.equals(provider)) {
                    providerList.append("Google");
                } else if (FacebookAuthProvider.PROVIDER_ID.equals(provider)) {
                    providerList.append("Facebook");
                } else if (EmailAuthProvider.PROVIDER_ID.equals(provider)) {
                    providerList.append("Password");
                } else {
                    providerList.append(provider);
                }

                if (providerIter.hasNext()) {
                    providerList.append(", ");
                }
            }
        }

        mEnabledProviders.setText(providerList);
    }

    private void populateIdpToken() {
        String token = null;
        String secret = null;
        if (mIdpResponse != null) {
            token = mIdpResponse.getIdpToken();
            secret = mIdpResponse.getIdpSecret();
        }
        if (token == null) {
            findViewById(R.id.idp_token_layout).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.idp_token)).setText(token);
        }
        if (secret == null) {
            findViewById(R.id.idp_secret_layout).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.idp_secret)).setText(secret);
        }
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG)
                .show();
    }

    public void verifyEmail(View v) {
        mButtonVerifyEmail.setEnabled(false);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignedInActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            mButtonVerifyEmail.setEnabled(false);
                            mButtonOk.setEnabled(true);
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(SignedInActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                            mButtonVerifyEmail.setEnabled(true);
                        }
                    }
                });
    }

    public void processOk(View v) {
        if (mActivityToStart != null) {
            startActivity(new Intent(this, mActivityToStart));
        }
        finish();
    }

    public static Intent createIntent(Context context, IdpResponse idpResponse) {
        Intent in = new Intent();
        in.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        in.setClass(context, SignedInActivity.class);
        return in;
    }
}
