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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.tasking.helper.SharedPrefsHelper;
import me.jakemoritz.tasking.misc.App;

public class SortTasklistTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "SortTasklistTask";

    public SortTasklistResponse delegate = null;

    Activity mActivity;
    String mEmail;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    List<Task> taskList;
    Tasks service;

    public SortTasklistTask(Activity mActivity, List<Task> taskList) {
        this.mActivity = mActivity;
        this.mEmail = SharedPrefsHelper.getInstance().getUserEmail();
        this.taskList = taskList;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            String token = fetchToken();
            if (token != null){
                credential = GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(TasksScopes.TASKS));
                credential.setSelectedAccountName(mEmail);
                service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName("Tasking").build();

                List<TaskList> tasklists = service.tasklists().list().execute().getItems();
                String firstTasklistId = tasklists.get(0).getId();

                List<Task> reversedList = new ArrayList<>();
                reversedList.addAll(taskList);
                Collections.reverse(reversedList);

                for (Task task : reversedList){
                    Task result = service.tasks().move(firstTasklistId, task.getId()).execute();
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
            return GoogleAuthUtil.getToken(mActivity, mEmail, App.TASK_OAUTH);
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
        delegate.tasksSorted();
    }
}
