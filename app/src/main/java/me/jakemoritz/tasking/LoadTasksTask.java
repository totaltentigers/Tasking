package me.jakemoritz.tasking;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LoadTasksTask extends AsyncTask<Void, Void, Void> {

    public AsyncResponse delegate = null;

    @Override
    protected void onPostExecute(Void aVoid) {
        delegate.processFinish(tasks);
    }

    private static final String TAG = "LoadTasksTask";

    private final static String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    Activity mActivity;
    String mScope;
    String mEmail;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    List<Task> tasks;
    Tasks service;

    public LoadTasksTask(Activity mActivity) {
        this.mActivity = mActivity;

        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("PREFS_ACC", 0);
        this.mEmail = sharedPreferences.getString("email", null);
    }

    // Executes asynchronous job.
    // Runs when you call execute() on an instance
    @Override
    protected Void doInBackground(Void... params) {
        //Log.d(TAG, "doInBackground");
        mScope = SCOPE;
        try {
            String token = fetchToken();
            if (token != null){
                credential = GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(TasksScopes.TASKS));
                credential.setSelectedAccountName(mEmail);
                service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName("Tasking").build();

                tasks = service.tasks().list("@default").execute().getItems();
            }
        } catch (IOException e){
            // The fetchToken() method handles Google-specific exceptions,
            // so there was an exception at a higher level.
            Log.d(TAG, e.toString());
        }
        return null;
    }

    // Fetches authentication token from Google and
    // handles GoogleAuthExceptions
    protected String fetchToken() throws IOException{
        try {
            Log.d(TAG, mActivity.toString() + ", " + mEmail + ", " + mScope);
            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
        } catch (UserRecoverableAuthException userRecoverableException){
            // GooglePlayServices.apk is either old, disabled, or not present.
            // so we must display a UI to recover.
            //mActivity.handleException(userRecoverableException);
            Log.e(TAG, userRecoverableException.toString());
        } catch (GoogleAuthException fatalException){
            Log.e(TAG, fatalException.toString());
        }
        return null;
    }
}
