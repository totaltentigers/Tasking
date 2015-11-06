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

public class UpdateTasklistTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "UpdateTasklistTask";

    public UpdateTasklistResponse delegate = null;

    @Override
    protected void onPostExecute(Void aVoid) {
        delegate.updateTasklistFinish();
    }

    Activity mActivity;
    String mScope = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    String mEmail;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    List<Task> taskList;
    Tasks service;

    public UpdateTasklistTask(Activity mActivity, List<Task> taskList) {
        this.mActivity = mActivity;

        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("PREFS_ACC", 0);
        this.mEmail = sharedPreferences.getString("email", null);

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

                List<Task> list = service.tasks().list("@default").execute().getItems();
                 for (Task task : list){
                    service.tasks().delete("@default", task.getId()).execute();
                }
                for (int i = taskList.size() - 1; i >= 0; i--){
                    Task newTask = new Task();
                    Task task = taskList.get(i);
                    newTask.setTitle(task.getTitle());
                    newTask.setNotes(task.getNotes());
                    newTask.setStatus(task.getStatus());
                    if (task.getDue() != null){
                        newTask.setDue(task.getDue());
                    }
                    if (task.getCompleted() != null){
                        newTask.setCompleted(task.getCompleted());
                    }
                    //task.setPosition(null);
                    Task result = service.tasks().insert("@default", newTask).execute();
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
