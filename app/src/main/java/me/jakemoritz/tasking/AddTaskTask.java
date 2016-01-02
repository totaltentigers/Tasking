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

public class AddTaskTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "AddTaskTask";

    public AddTaskResponse delegate = null;

    Activity mActivity;
    String mEmail;
    Task task;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    Tasks service;

    public AddTaskTask(Activity mActivity, Task task) {
        this.mActivity = mActivity;
        this.task = task;

        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(mActivity.getString(R.string.shared_prefs_account), 0);
        this.mEmail = sharedPreferences.getString(mActivity.getString(R.string.shared_prefs_email), null);
    }

    // Executes asynchronous job.
    // Runs when you call execute() on an instance
    @Override
    protected Void doInBackground(Void... params) {
        try {
            String token = fetchToken();
            if (token != null){
                credential = GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(TasksScopes.TASKS));
                credential.setSelectedAccountName(mEmail);
                service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName(mActivity.getString(R.string.app_name)).build();

                //Task result = service.tasks().insert("@default", task).execute();
                DatabaseHelper dbHelper = new DatabaseHelper(mActivity);
                dbHelper.insertTask(task);
                dbHelper.close();
            }
        } catch (IOException e){
            // The fetchToken() method handles Google-specific exceptions,
            // so there was an exception at a higher level.
            Log.d(TAG, "error");

            Log.d(TAG, e.toString());
        }
        return null;
    }

    // Fetches authentication token from Google and
    // handles GoogleAuthExceptions
    protected String fetchToken() throws IOException{
        try {
            return GoogleAuthUtil.getToken(mActivity, mEmail, mActivity.getString(R.string.update_task_oathscope));
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

    @Override
    protected void onPostExecute(Void aVoid) {
        if (delegate != null){
            delegate.addTaskFinish();
        }
    }
}
