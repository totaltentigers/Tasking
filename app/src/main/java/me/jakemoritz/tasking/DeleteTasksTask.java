package me.jakemoritz.tasking;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;

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
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DeleteTasksTask extends AsyncTask<Void, Void, Void> {

    public DeleteTasksResponse delegate = null;

    @Override
    protected void onPostExecute(Void aVoid) {
        delegate.deleteTasksFinish(previousTasks);
    }

    private static final String TAG = "DeleteTasksTask";

    private final static String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    Activity mActivity;
    String mScope;
    String mEmail;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    List<Task> tasks;
    TaskList previousTasks;
    Tasks service;
    SparseBooleanArray mSelectedItemIds;

    public DeleteTasksTask(Activity mActivity, SparseBooleanArray mSelectedItemIds) {
        this.mActivity = mActivity;
        this.mSelectedItemIds = mSelectedItemIds;

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

                previousTasks = service.tasklists().get("@default").execute();


                for (int i = 0; i < mSelectedItemIds.size(); i++){
                    Task task = tasks.get(mSelectedItemIds.keyAt(i));
                    service.tasks().delete("@default", task.getId()).execute();
                }
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
