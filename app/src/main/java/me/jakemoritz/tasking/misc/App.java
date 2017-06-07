package me.jakemoritz.tasking.misc;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class App extends Application {

    private static App mInstance;

    public static final String TASK_SCOPE = "https://www.googleapis.com/auth/tasks";
    public static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    public static final String TASK_OAUTH = "oauth2:https://www.googleapis.com/auth/userinfo.profile";


    public static synchronized App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }
}
