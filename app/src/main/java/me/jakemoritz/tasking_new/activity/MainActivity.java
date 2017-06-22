package me.jakemoritz.tasking_new.activity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.jakemoritz.tasking_new.R;
import me.jakemoritz.tasking_new.api.retrofit.GoogleEndpointInterface;
import me.jakemoritz.tasking_new.api.retrofit.PlusPersonDeserializer;
import me.jakemoritz.tasking_new.fragment.SettingsFragment;
import me.jakemoritz.tasking_new.fragment.TaskListFragment;
import me.jakemoritz.tasking_new.helper.SharedPrefsHelper;
import me.jakemoritz.tasking_new.misc.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Constants for cover image GET request
    private static final String COVER_IMAGE_BASE_URL = "https://www.googleapis.com/plus/v1/people/";

    // Client used to interact with Google APIs
    private static GoogleApiClient mGoogleApiClient;

    // Views
    private ImageView navUserAvatar;
    private LinearLayout navUserCover;

    private boolean wantToLoadUserImages;
    private boolean wantToSignOut;

    private MenuItem selectedMenuItem;

    private ActionBarDrawerToggle toggle;
    private boolean toolbarNavigationListenerRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getCallingActivity() != null && getCallingActivity().getClassName().matches(LoginActivity.class.getName()) && !SharedPrefsHelper.getInstance().isLoginSnackbarShown()) {
            // User signed in for the first time
            Snackbar signInSuccessSnackbar = Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.auth_success) + SharedPrefsHelper.getInstance().getUserEmail(), Snackbar.LENGTH_LONG);
            signInSuccessSnackbar.show();

            SharedPrefsHelper.getInstance().setLoginSnackbarShown(true);
        }

        // Configure sign-in to request the user'userImageTarget ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(App.TASK_SCOPE), new Scope(App.PROFILE_SCOPE))
                .build();

        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .build();

        // Initialize views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (selectedMenuItem != null) {
                    // Handle navigation view item clicks here.
                    int id = selectedMenuItem.getItemId();

                    // Check if selected Fragment is already active
                    if (!((id == R.id.nav_tasks) && (getFragmentManager().findFragmentById(R.id.content_main) instanceof TaskListFragment)) &&
                            !((id == R.id.nav_settings) && (getFragmentManager().findFragmentById(R.id.content_main) instanceof SettingsFragment))) {
                        if (id == R.id.nav_tasks) {
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_main, TaskListFragment.newInstance())
                                    .commit();
                        } else if (id == R.id.nav_settings) {
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_main, SettingsFragment.newInstance())
                                    .commit();
                        }
                    }
                }
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Inflate nav drawer header layout
        LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView);

        TextView navUserName = (TextView) navigationView.findViewById(R.id.user_name);
        TextView navUserEmail = (TextView) navigationView.findViewById(R.id.user_email);
        navUserAvatar = (ImageView) navigationView.findViewById(R.id.user_avatar);
        navUserCover = (LinearLayout) navigationView.findViewById(R.id.user_cover);

        navUserName.setText(SharedPrefsHelper.getInstance().getUserDisplayName());
        navUserEmail.setText(SharedPrefsHelper.getInstance().getUserEmail());
        setNavUserImage(getString(R.string.user_image));
        setNavUserImage(getString(R.string.user_cover_image));

        navigationView.getMenu().getItem(0).setChecked(true);

        wantToLoadUserImages = true;

        // Connect to update user images
        mGoogleApiClient.connect();

        Fragment defaultFragment = null;
        if (savedInstanceState != null) {
            defaultFragment = getFragmentManager().getFragment(savedInstanceState, "current_fragment");
        }

        if (defaultFragment == null) {
            defaultFragment = TaskListFragment.newInstance();
        }

        // Initialize default fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.content_main, defaultFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.content_main);
        getFragmentManager().putFragment(outState, "current_fragment", currentFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void downloadUserImage(Uri imageUri, final String filename) {
        Target imageTarget = null;

        if (filename.matches(getString(R.string.user_image))) {
            imageTarget = userImageTarget;
        } else if (filename.matches(getString(R.string.user_cover_image))) {
            imageTarget = userCoverImageTarget;
        }

        if (imageTarget != null) {
            Picasso.with(App.getInstance()).load(imageUri).into(imageTarget);
        }
    }

    Target userImageTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            saveImageToFile(bitmap, getString(R.string.user_image));
            navUserAvatar.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    Target userCoverImageTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            saveImageToFile(bitmap, getString(R.string.user_cover_image));

            // Draw black overlay to darken cover image
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Paint darken = new Paint();
            darken.setColor(Color.BLACK);
            darken.setAlpha(100);


            Canvas c = new Canvas(mutableBitmap);
            c.drawPaint(darken);
            navUserCover.setBackground(new BitmapDrawable(getResources(), mutableBitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    private void getUserCoverImageUrl() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, new PlusPersonDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(COVER_IMAGE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GoogleEndpointInterface googleEndpointInterface = retrofit.create(GoogleEndpointInterface.class);
        final Call<String> coverImageURL = googleEndpointInterface.getCoverImageURL(SharedPrefsHelper.getInstance().getUserId(), getString(R.string.google_api_key_debug));
        coverImageURL.enqueue(new Callback<String>() {

            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    String coverPhotoUrl = response.body();

                    if (coverPhotoUrl != null) {
                        Uri imageUri = Uri.parse(coverPhotoUrl);

                        if (imageUri != null) {
                            downloadUserImage(imageUri, getString(R.string.user_cover_image));
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void saveImageToFile(Bitmap bitmap, String filename) {
        FileOutputStream fos = null;
        try {
            File file = new File(getCacheDir(), filename + ".jpg");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setNavUserImage(final String filename) {
        // Create Target asynchronously load and set image
        Picasso.with(this).load(new File(getCacheDir() + File.separator + filename + ".jpg")).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (filename.matches(getString(R.string.user_image))) {
                    navUserAvatar.setImageBitmap(bitmap);
                } else if (filename.matches(getString(R.string.user_cover_image))) {
                    // Draw black overlay to darken cover image
                    Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Paint darken = new Paint();
                    darken.setColor(Color.BLACK);
                    darken.setAlpha(100);


                    Canvas c = new Canvas(mutableBitmap);
                    c.drawPaint(darken);
                    navUserCover.setBackground(new BitmapDrawable(getResources(), mutableBitmap));
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void enableUpNavigation(boolean enable) {
        if (enable) {
            toggle.setDrawerIndicatorEnabled(false);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            if (!toolbarNavigationListenerRegistered) {
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });

                toolbarNavigationListenerRegistered = true;
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(null);
            toolbarNavigationListenerRegistered = false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Save selected menu item to check if already in selected Fragment
        selectedMenuItem = item;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Intent received for sign out
        if (intent.hasExtra("signOut") && intent.getBooleanExtra("signOut", false)) {
            wantToSignOut = true;

            // Connect GoogleApiClient before signing out
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                signOut();
            }
        }
    }

    private void signOut() {
        // Sign out button clicked
        // Sign out and disconnect user'userImageTarget Google account
        // Clear app data
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);

        clearAppData();
        SharedPrefsHelper.getInstance().setLoggedIn(false);
        wantToSignOut = false;

        SharedPrefsHelper.getInstance().setSignOutSnackbarShown(false);

        // Return to intro screen
        Intent signOutIntent = new Intent(App.getInstance(), LoginActivity.class);
        signOutIntent.putExtra("justSignedOut", true);
        App.getInstance().startActivity(signOutIntent);

        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (wantToSignOut) {
            signOut();
        } else {
            // Connected to update user images
            OptionalPendingResult<GoogleSignInResult> optionalPendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);

            optionalPendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    if (wantToLoadUserImages) {
                        if (googleSignInResult.isSuccess()) {
                            GoogleSignInAccount acct = googleSignInResult.getSignInAccount();

                            if (acct != null && acct.getPhotoUrl() != null && !acct.getPhotoUrl().toString().isEmpty()) {
                                downloadUserImage(acct.getPhotoUrl(), getString(R.string.user_image));
                            }
                        }

                        getUserCoverImageUrl();
                        wantToLoadUserImages = false;
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    // Iterates though all files in cache directory
    private void clearAppData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists() && appDir.isDirectory()) {
            for (String fileName : appDir.list()) {
                if (!fileName.equals("lib")) {
                    deleteDir(new File(appDir, fileName));
                }
            }
        }
    }

    // Recursively deletes files and folders
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            for (String fileName : dir.list()) {
                boolean success = deleteDir(new File(dir, fileName));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    
}
