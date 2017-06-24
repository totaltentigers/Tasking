package me.jakemoritz.tasking_new.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import me.jakemoritz.tasking_new.R;
import me.jakemoritz.tasking_new.dialog.PermissionRationaleDialogFragment;
import me.jakemoritz.tasking_new.helper.PermissionHelper;
import me.jakemoritz.tasking_new.helper.SharedPrefsHelper;
import me.jakemoritz.tasking_new.misc.App;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getIntent().hasExtra("justSignedOut") && getIntent().getBooleanExtra("justSignedOut", false) && findViewById(R.id.activity_login) != null && !SharedPrefsHelper.getInstance().isSignOutSnackbarShown()) {
            Snackbar.make(findViewById(R.id.activity_login), R.string.just_signed_out, Snackbar.LENGTH_LONG).show();

            SharedPrefsHelper.getInstance().setSignOutSnackbarShown(true);
        }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(App.TASK_SCOPE), new Scope(App.PROFILE_SCOPE))
                .build();

        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();

        // Set fullscreen
        boolean apiGreaterThanOrEqual19 = (Build.VERSION.SDK_INT >= 19);

        int mUIFlag;

        if (apiGreaterThanOrEqual19) {
            mUIFlag = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        } else {
            mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case (PermissionHelper.GET_ACCOUNTS_REQ_CODE):
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    onSignInClicked();
                } else {
                    // Permission denied
                    PermissionRationaleDialogFragment permissionRationaleDialogFragment = PermissionRationaleDialogFragment.newInstance(this, Manifest.permission.GET_ACCOUNTS, getString(R.string.get_accounts_permission_denied));
                    permissionRationaleDialogFragment.show(getFragmentManager(), PermissionRationaleDialogFragment.class.getSimpleName());
                }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) {
            if (PermissionHelper.getInstance().permissionGranted(this, Manifest.permission.GET_ACCOUNTS)) {
                onSignInClicked();
            } else {
                PermissionHelper.getInstance().requestPermission(this, Manifest.permission.GET_ACCOUNTS);
            }
        }
    }

    private void onSignInClicked() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            saveUserInfo(acct);

            SharedPrefsHelper.getInstance().setLoginSnackbarShown(false);

            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivityForResult(mainIntent, 0);

            finish();
        } else {
            // Sign in failed, show unauthenticated UI
            Snackbar signInErrorSnackbar = Snackbar.make(findViewById(R.id.activity_login), R.string.auth_error, Snackbar.LENGTH_LONG);
            signInErrorSnackbar.show();
        }
    }

    private void saveUserInfo(GoogleSignInAccount acct) {
        // Save sign-in state and user info
        SharedPrefsHelper.getInstance().setUserEmail(acct.getEmail());
        SharedPrefsHelper.getInstance().setLoggedIn(true);
        SharedPrefsHelper.getInstance().setUserDisplayName(acct.getDisplayName());
        SharedPrefsHelper.getInstance().setUserId(acct.getId());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}

