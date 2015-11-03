package me.jakemoritz.tasking;

import android.content.Context;
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
import android.os.AsyncTask;
import android.os.Bundle;
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
        implements NavigationView.OnNavigationItemSelectedListener, AccountDialogPreference.OnSignOutListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MainActivity";

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private boolean signingOut = false;
    private boolean updatingUserInfo = false;
    private View header;

    ImageView navUserAvatar;
    TextView navUserName;
    TextView navUserEmail;
    LinearLayout navUserCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        navigationView.addHeaderView(header);

        navUserAvatar = (ImageView) header.findViewById(R.id.user_avatar);
        navUserName = (TextView) header.findViewById(R.id.user_name);
        navUserEmail = (TextView) header.findViewById(R.id.user_email);
        navUserCover = (LinearLayout) header.findViewById(R.id.user_cover);

        loadNavUserInfo();

        // Initialize default fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new TaskListFragment())
                .commit();
    }

    public GoogleApiClient connectApiClientForUserInfo(){
        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .build();

        //updatingUserInfo = true;
        mGoogleApiClient.connect();
        return mGoogleApiClient;
    }

    public void loadNavUserInfo(){
        loadNavUserName();
        loadNavUserEmail();
        loadNavUserImage();
        loadNavUserCoverImage();
    }

    public void loadNavUserName(){
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS_ACC", 0);
        String name = sharedPreferences.getString("name", "");

        navUserName.setText(name);
    }

    public void loadNavUserEmail(){
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS_ACC", 0);
        String email = sharedPreferences.getString("email", "");

        navUserEmail.setText(email);
    }

    public void saveImageToFile(Bitmap bitmap, String filename){
        FileOutputStream outputStream;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bitmapByteArray = byteArrayOutputStream.toByteArray();
        bitmap.recycle();

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(bitmapByteArray);
            outputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Bitmap loadImageFromFile(String filename){
        // Try to load image from file
        FileInputStream inputStream = null;
        File file = new File(getFilesDir() + "/" + filename);

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException fileNotFoundException){
            return null;
        } catch (Exception e){
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(inputStream);
    }

    public void loadNavUserImage(){
        // If an image is loaded, pass it
        if (loadImageFromFile("user_image") != null){
            navUserAvatar.setImageBitmap(loadImageFromFile("user_image"));
        }
        // If no image is loaded, pull from servers
        else {
            GoogleApiClient mGoogleApiClient = connectApiClientForUserInfo();
            Log.d(TAG, String.valueOf(mGoogleApiClient == null));
            if (mGoogleApiClient != null){
                if (mGoogleApiClient.isConnected()){
                    Person user = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                    if (user != null){
                        new AsyncTask<String, Void, Bitmap>(){
                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
                                Bitmap circle = getCircleBitmap(bitmap);
                                Bitmap scaled = Bitmap.createScaledBitmap(circle, 168, 168, true);
                                navUserAvatar.setImageBitmap(scaled);

                                saveImageToFile(scaled, "user_image");
                            }

                            @Override
                            protected Bitmap doInBackground(String... params) {
                                try {
                                    URL url = new URL(params[0]);
                                    InputStream in = url.openStream();
                                    return BitmapFactory.decodeStream(in);
                                } catch (Exception e){
                                    Log.e(TAG, e.toString());
                                }
                                return null;
                            }
                        }.execute(user.getImage().getUrl());
                    }
                }
            }

        }
    }

    public void loadNavUserCoverImage(){
        final Paint darken = new Paint();
        darken.setColor(Color.BLACK);
        darken.setAlpha(100);

        // If an image is loaded, pass it
        if (loadImageFromFile("user_cover") != null){
            Bitmap bitmapCopy = loadImageFromFile("user_cover").copy(Bitmap.Config.ARGB_8888, true);
            Canvas c = new Canvas(bitmapCopy);
            c.drawPaint(darken);
            navUserCover.setBackground(new BitmapDrawable(getResources(), bitmapCopy));
        }
        // If no image is loaded, pull from servers
        else {
            GoogleApiClient mGoogleApiClient = connectApiClientForUserInfo();
            if (mGoogleApiClient != null){
                if (mGoogleApiClient.isConnected()){
                    Person user = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                    if (user != null){
                        new AsyncTask<String, Void, Bitmap>(){
                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
                                Bitmap bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                Canvas c = new Canvas(bitmapCopy);
                                c.drawPaint(darken);
                                navUserCover.setBackground(new BitmapDrawable(getResources(), bitmapCopy));

                                saveImageToFile(bitmap, "user_cover");
                            }

                            @Override
                            protected Bitmap doInBackground(String... params) {
                                try {
                                    URL url = new URL(params[0]);
                                    InputStream in = url.openStream();
                                    return BitmapFactory.decodeStream(in);
                                } catch (Exception e){
                                    Log.e(TAG, e.toString());
                                }
                                return null;
                            }
                        }.execute(user.getCover().getCoverPhoto().getUrl());
                    }
                }
            }
        }
    }

    public void signOut(){
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        mGoogleApiClient.disconnect();

        signingOut = false;

        // Save sign-in state
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS_ACC", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("signedIn", false);
        editor.commit();

        startActivity(new Intent(this, HelperActivity.class));
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_tasks) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_main, new TaskListFragment())
                    .commit();
        }
        else if (id == R.id.nav_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_main, new SettingsFragment())
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void connectApiClientForSignOut() {
        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .build();

        signingOut = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;

        if (signingOut){
            signOut();
        }
        else if (updatingUserInfo){
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap){
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

        return output;
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

        if (!mIsResolving && mShouldResolve){
            if (connectionResult.hasResolution()){
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e){
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.

            }
        } else {
            // Show the signed-out UI

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
