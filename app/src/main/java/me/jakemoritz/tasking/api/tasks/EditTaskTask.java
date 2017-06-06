package me.jakemoritz.tasking.api.tasks;

import android.app.Activity;
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
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.tasking.R;
import me.jakemoritz.tasking.database.DatabaseHelper;
import me.jakemoritz.tasking.helper.SharedPrefsHelper;

public class EditTaskTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "EditTaskTask";

    public EditTaskResponse delegate = null;

    Activity mActivity;
    String mEmail;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    Task task;
    Tasks service;

    public EditTaskTask(Activity mActivity, Task task) {
        this.mActivity = mActivity;
        this.task = task;
        this.mEmail = SharedPrefsHelper.getInstance().getUserEmail();
    }

    // Executes asynchronous job.
    // Runs when you call execute() on an instance
    @Override
    protected Void doInBackground(Void... params) {
        //Log.d(TAG, "doInBackground");
        try {
            String token = fetchToken();
            if (token != null){
                credential = GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(TasksScopes.TASKS));
                credential.setSelectedAccountName(mEmail);
                service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName(mActivity.getString(R.string.app_name)).build();

                List<TaskList> tasklists = service.tasklists().list().execute().getItems();
                String firstTasklistId = tasklists.get(0).getId();

                Task result = service.tasks().update(firstTasklistId, task.getId(), task).execute();

                DatabaseHelper dbHelper = new DatabaseHelper(mActivity);
                dbHelper.updateTaskInDb(task.getId(), task);
                dbHelper.close();
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
        delegate.editTaskFinish();
    }
}
