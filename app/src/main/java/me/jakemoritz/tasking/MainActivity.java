package me.jakemoritz.tasking;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private static GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

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
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
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

                    if ((id == R.id.nav_tasks) && (getFragmentManager().findFragmentById(R.id.content_main) instanceof TaskListFragment)) {
                    } else if ((id == R.id.nav_settings) && (getFragmentManager().findFragmentById(R.id.content_main) instanceof SettingsFragment)) {
                    } else {
                        // set colors of items
                        for (int i = 0; i < navigationView.getMenu().size(); i++) {
                            // if menu item is the selected item
                            if (navigationView.getMenu().getItem(i).getItemId() == selectedMenuItem.getItemId()) {
                                setNavItemColorToPrimary(i);
                            } else {
                                resetNavItemColor(i);
                            }
                        }

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

        loadNavUserName();
        loadNavUserEmail();

        navigationView.getMenu().getItem(0).setChecked(true);
        setNavItemColorToPrimary(0);
        resetNavItemColor(1);

        // Initialize default fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new TaskListFragment())
                .commit();
    }

    public void resetNavItemColor(int position) {
        Drawable taskMenuItemIcon = navigationView.getMenu().getItem(position).getIcon();
        taskMenuItemIcon.mutate().setColorFilter(0x8C000000, PorterDuff.Mode.MULTIPLY);
        navigationView.getMenu().getItem(position).setIcon(taskMenuItemIcon);
    }

    public void setNavItemColorToPrimary(int position) {
        // change selected item icon to app primary color
        Drawable icon;
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;

        Drawable taskMenuItemIcon = navigationView.getMenu().getItem(position).getIcon();
        taskMenuItemIcon.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        navigationView.getMenu().getItem(position).setIcon(taskMenuItemIcon);
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
        FileOutputStream outputStream;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bitmapByteArray = byteArrayOutputStream.toByteArray();
        bitmap.recycle();
        String filepath = getCacheDir() + File.separator + filename;
        try {
            outputStream = new FileOutputStream(new File(filepath), true);
            outputStream.write(bitmapByteArray);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap loadImageFromFile(String filename) {
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

    public void loadNavUserImageFromServer() {
        // First attempt to update images from server
        Person user = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (user != null && user.getImage() != null) {
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);

                        Bitmap userImage = getCircleBitmap(bitmap);
                        navUserAvatar.setImageBitmap(userImage);
                        saveImageToFile(copy, getString(R.string.user_image));
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
            }.execute(user.getImage().getUrl().substring(0, user.getImage().getUrl().length() - 2) + 400);
        }
    }

    public void loadNavUserImage() {
        if (loadImageFromFile(getString(R.string.user_image)) != null) {
            navUserAvatar.setImageBitmap(getCircleBitmap(loadImageFromFile(getString(R.string.user_image))));
            // attempt to update image
            loadNavUserImageFromServer();
        } else {
            // If no file found, load from server
            loadNavUserImageFromServer();
        }
    }

    public void loadNavUserCoverImageFromServer() {
        final Paint darken = new Paint();
        darken.setColor(Color.BLACK);
        darken.setAlpha(100);

        // First attempt to update images from server
        Person user = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
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
        }
    }

    public void loadNavUserCoverImage() {
        final Paint darken = new Paint();
        darken.setColor(Color.BLACK);
        darken.setAlpha(100);

        if (loadImageFromFile(getString(R.string.user_cover_image)) != null) {
            Bitmap bitmapCopy = loadImageFromFile(getString(R.string.user_cover_image)).copy(Bitmap.Config.ARGB_8888, true);
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
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);

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
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;

        if (wantToLoadUserImages) {
            loadNavUserImage();
            loadNavUserCoverImage();
        }
        if (wantToSignOut) {
            signOutHelper();
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        return Bitmap.createScaledBitmap(output, px, px, true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Couldn't connect to Google Play Services. The user needs to select an account,
//        grant permissions or resolve an error in order to sign in. Refer to the javadoc for
//        Connection Result to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult." , e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                Snackbar.make(findViewById(R.id.activity_login), getString(R.string.gpservices_conn_fail), Snackbar.LENGTH_INDEFINITE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }
}
