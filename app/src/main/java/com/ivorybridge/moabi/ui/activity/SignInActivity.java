package com.ivorybridge.moabi.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.ivorybridge.moabi.R;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This class takes care of everything that happens during a sign-in process,
 * whereby a user decides to sign in rather than to stay anonymous.
 * The activity is based on Firebase UI - Auth
 * (https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md)
 * If a user is already signed in, the user is redirected to the MainDashboardActivity.
 * Otherwise, there are 4 authentication methods available, Facebook, Google, Phone and E-mail.
 */
public class SignInActivity extends AppCompatActivity {

    /**
     * Arbitrary request code value
     */
    private static final int RC_SIGN_IN = 115;

    /**
     * Tag for debugging purposes.
     */
    private static final String TAG = SignInActivity.class.getSimpleName();
    private FirebaseAuth mAuth;

    /**
     * URL to google terms of service.
     */
    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            //asyncCallsMasterRepository.makeCallsToConnectedServices();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    // proceed to main activity after 2 seconds if the user is signed in.
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    // close splash activity
                    finish();
                }
            });
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    AuthUI.IdpConfig phoneConfigWithDefaultNumber = new AuthUI.IdpConfig.PhoneBuilder()
                            .build();

                    Log.i(TAG, "User is not logged in. Initiating Sign-in process.");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setLogo(R.drawable.logo_transparent_background)
                                    .setTheme(R.style.LoginTheme)
                                    .setIsSmartLockEnabled(false)
                                    //.setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                                    new AuthUI.IdpConfig.FacebookBuilder().build(),
                                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                                    phoneConfigWithDefaultNumber))
                                    .build(),
                            RC_SIGN_IN);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the requestCode you passed into startActivityForResult(...)
        // when starting the sign in flow.

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    onBackPressed();
                    return;
                }

                if (response.getError() != null) {
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        showSnackbar(R.string.no_internet_connection);
                        return;
                    }
                }

                /*
                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }*/

                showSnackbar(R.string.unknown_sign_in_response);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param resId The Snackbar textID.
     */
    private void showSnackbar(final int resId) {
        View container = findViewById(R.id.activity_signin_relativelayout);
        if (container != null) {
            Snackbar.make(container, resId, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SignInActivity.this, IntroActivity.class));
    }
}

