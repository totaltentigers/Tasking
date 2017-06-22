package me.jakemoritz.tasking.helper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import me.jakemoritz.tasking.R;
import me.jakemoritz.tasking.misc.App;

public class SharedPrefsHelper {

    private static SharedPrefsHelper prefsHelper;
    private SharedPreferences sharedPrefs;

    public synchronized static SharedPrefsHelper getInstance(){
        if (prefsHelper == null){
            prefsHelper = new SharedPrefsHelper();
            prefsHelper.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        }
        return prefsHelper;
    }

    public String getUserEmail(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_email), "");
    }

    public void setUserEmail(String userEmail){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_email), userEmail);
        editor.apply();
    }

    public String getUserDisplayName(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_name), "");
    }

    public void setUserDisplayName(String userDisplayName){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_name), userDisplayName);
        editor.apply();
    }

    public String getUserId(){
        return sharedPrefs.getString(App.getInstance().getString(R.string.shared_prefs_id), "");
    }

    public void setUserId(String userId){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(App.getInstance().getString(R.string.shared_prefs_id), userId);
        editor.apply();
    }

    public Boolean isLoggedIn(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.shared_prefs_logged_in), false);
    }

    public void setLoggedIn(Boolean loggedIn){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.shared_prefs_logged_in), loggedIn);
        editor.apply();
    }

    public Boolean isLoginSnackbarShown(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.shared_prefs_login_snackbar_shown), true);
    }

    public void setLoginSnackbarShown(Boolean loginSnackbarShown){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.shared_prefs_login_snackbar_shown), loginSnackbarShown);
        editor.apply();
    }

    public Boolean isSignOutSnackbarShown(){
        return sharedPrefs.getBoolean(App.getInstance().getString(R.string.shared_prefs_sign_out_snackbar_shown), true);
    }

    public void setSignOutSnackbarShown(Boolean signOutSnackbarShown){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(App.getInstance().getString(R.string.shared_prefs_sign_out_snackbar_shown), signOutSnackbarShown);
        editor.apply();
    }
}
