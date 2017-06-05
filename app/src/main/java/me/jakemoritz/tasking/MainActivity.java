package me.jakemoritz.tasking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.squareup.picasso.Picasso.with;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private static GoogleApiClient mGoogleApiClient;

    // Declare variables for views
    ImageView navUserAvatar;
    TextView navUserName;
    TextView navUserEmail;
    LinearLayout navUserCover;

    NavigationView navigationView;

    boolean wantToLoadUserImages;

    boolean wantToSignOut;

    private MenuItem selectedMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(getString(R.string.gac_task_scope)))
                .build();

        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        wantToLoadUserImages = true;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (selectedMenuItem != null) {
                    // Handle navigation view item clicks here.
                    int id = selectedMenuItem.getItemId();

                    if (!((id == R.id.nav_tasks) && (getFragmentManager().findFragmentById(R.id.content_main) instanceof TaskListFragment)) &&
                            !((id == R.id.nav_settings) && (getFragmentManager().findFragmentById(R.id.content_main) instanceof SettingsFragment))) {
                        if (id == R.id.nav_tasks) {
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_main, new TaskListFragment())
                                    .commit();
                        } else if (id == R.id.nav_settings) {
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_main, new SettingsFragment())
                                    .commit();
                        }
                    }
                }
            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView);

        navUserAvatar = (ImageView) header.findViewById(R.id.user_avatar);
        navUserName = (TextView) header.findViewById(R.id.user_name);
        navUserEmail = (TextView) header.findViewById(R.id.user_email);
        navUserCover = (LinearLayout) header.findViewById(R.id.user_cover);

        connectGoogleApiClient();
        loadNavUserName();
        loadNavUserEmail();

        navigationView.getMenu().getItem(0).setChecked(true);

        // Initialize default fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new TaskListFragment())
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void connectGoogleApiClient() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void loadNavUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
        String name = sharedPreferences.getString(getString(R.string.shared_prefs_name), "");

        navUserName.setText(name);
    }

    public void loadNavUserEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
        String email = sharedPreferences.getString(getString(R.string.shared_prefs_email), "");

        navUserEmail.setText(email);
    }

    public void saveImageToFile(Bitmap bitmap, String filename) {
        FileOutputStream fos = null;
        try {
            File file = new File(getCacheDir(), filename + ".jpg");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);

            setNavUserImage(filename);
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

        bitmap.recycle();
    }

    public Bitmap loadBitmapFromFile(String filename) {
        // Try to load image from file
        FileInputStream inputStream = null;
        File file = new File(getCacheDir() + File.separator + filename);

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException fileNotFoundException) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(inputStream);
    }

    public void downloadUserImage(GoogleSignInResult googleSignInResult, final String filename) {
        // First attempt to update images from server
        GoogleSignInAccount acct = googleSignInResult.getSignInAccount();

        if (acct != null && acct.getPhotoUrl() != null) {
            new AsyncTask<Uri, Void, Bitmap>() {
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        saveImageToFile(bitmap, filename);
                    }
                }

                @Override
                protected Bitmap doInBackground(Uri... params) {
                    try {
                        return with(getApplicationContext()).load(params[0]).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(new Uri[]{acct.getPhotoUrl()});
        }
    }

    private void setNavUserImage(final String filename){
        Picasso.with(this).load(new File(getCacheDir() + File.separator + filename)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (filename.matches(getString(R.string.user_image))){
                    navUserAvatar.setImageBitmap(bitmap);
                } else if (filename.matches(getString(R.string.user_cover_image))){
                    navUserCover.setBackground(new BitmapDrawable(getResources(), bitmap));
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

    public void loadNavUserImage(GoogleSignInResult googleSignInResult, String filename) {
        setNavUserImage(filename);

        // Attempt to download image
        downloadUserImage(googleSignInResult, filename);
    }

    public void loadNavUserCoverImageFromServer() {
        final Paint darken = new Paint();
        darken.setColor(Color.BLACK);
        darken.setAlpha(100);

        // First attempt to update images from server
/*        Person user = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (user != null && user.getCover() != null) {
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);

                        Bitmap bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        Canvas c = new Canvas(bitmapCopy);
                        c.drawPaint(darken);
                        navUserCover.setBackground(new BitmapDrawable(getResources(), bitmapCopy));

                        saveImageToFile(copy, getString(R.string.user_cover_image));
                    }
                }

                @Override
                protected Bitmap doInBackground(String... params) {
                    try {
                        URL url = new URL(params[0]);
                        InputStream in = url.openStream();
                        return BitmapFactory.decodeStream(in);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    return null;
                }
            }.execute(user.getCover().getCoverPhoto().getUrl());
        }*/
    }

    public void loadNavUserCoverImage() {
        final Paint darken = new Paint();
        darken.setColor(Color.BLACK);
        darken.setAlpha(100);

        if (loadBitmapFromFile(getString(R.string.user_cover_image)) != null) {
            Bitmap bitmapCopy = loadBitmapFromFile(getString(R.string.user_cover_image)).copy(Bitmap.Config.ARGB_8888, true);
            Canvas c = new Canvas(bitmapCopy);
            c.drawPaint(darken);
            navUserCover.setBackground(new BitmapDrawable(getResources(), bitmapCopy));

            // attempt to update user image from server
            loadNavUserCoverImageFromServer();
        } else {
            // if no file found, pull from server
            loadNavUserCoverImageFromServer();
        }
        wantToLoadUserImages = false;
    }

    public void signOutHelper() {
//        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);

        clearAppData();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.shared_prefs_logged_in), false);
        editor.commit();

        wantToSignOut = false;
        startActivity(new Intent(this, HelperActivity.class));
    }

    public void clearAppData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i(TAG, "File /data/data/me.jakemoritz.tasking/" + s + " DELETED");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public void signOut() {
        wantToSignOut = true;
        mGoogleApiClient.connect();
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectedMenuItem = item;


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (wantToLoadUserImages) {
                loadNavUserImage(result, getString(R.string.user_image));

                wantToLoadUserImages = false;
            }
        }
    }
}
