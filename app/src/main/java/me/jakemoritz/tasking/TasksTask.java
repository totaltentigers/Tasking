package me.jakemoritz.tasking;

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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TasksTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "TasksTask";

    private final static String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    Activity mActivity;
    String mScope;
    String mEmail;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    List<String> tasksList;
    Tasks service;

    public TasksTask TasksTask(Activity mActivity, String mScope, String mEmail) {
        this.mActivity = mActivity;
        this.mScope = mScope;
        this.mEmail = mEmail;

        return this;
    }



    // Executes asynchronous job.
    // Runs when you call execute() on an instance
    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground");
        mScope = SCOPE;

        try {
            String token = fetchToken();
            if (token != null){
                credential = GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(TasksScopes.TASKS));
                credential.setSelectedAccountName(mEmail);
                service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName("Tasking").build();

                addTask();
/*                tasksList = new ArrayList<String>();

                List<Task> tasks = service.tasks().list("@default").execute().getItems();

                for (Task eachTask : tasks){
                    Log.d(TAG, eachTask.toPrettyString());
                }

                Task task = new Task();
                task.setTitle("Poopy");
                task.setNotes("Note notey");
                task.setDue(new DateTime(new Date(2015, 11, 2), TimeZone.getDefault()));
                Task result = service.tasks().insert("@default", task).execute();*/
            }
        } catch (IOException e){
            // The fetchToken() method handles Google-specific exceptions,
            // so there was an exception at a higher level.
            Log.d(TAG, e.toString());
        }
        return null;
    }

    public void addTask(){

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
