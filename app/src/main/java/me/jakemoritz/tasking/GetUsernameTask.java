package me.jakemoritz.tasking;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GetUsernameTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetUsernameTask";

    Activity mActivity;
    String mScope;
    String mEmail;

    public GetUsernameTask(Activity mActivity, String mScope, String mEmail) {
        this.mActivity = mActivity;
        this.mScope = mScope;
        this.mEmail = mEmail;
    }

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    List<String> tasksList;
    ArrayAdapter<String> adapter;
    Tasks service;

    private final static String SERVICE ="471356341761-m10js67m6hdgohjdic4ab23n2973ugrp.apps.googleusercontent.com";
    private final static String CLIENT = "471356341761-ns1j3gj0h8nq0kf8f34davsa937vhmm0.apps.googleusercontent.com";
    private final static String NEWSCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    // Executes asynchronous job.
    // Runs when you call execute() on an instance
    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground");
        try {
            Log.d(TAG, "initial mScope: " + mScope);
            mScope = "oath2:server:client_id:" + CLIENT + ":api_scope:https://www.googleapis.com/auth/tasks";
            mScope = NEWSCOPE;
            Log.d(TAG, "new mScope: " + mScope);

            String token = fetchToken();
            Log.d(TAG, "token null? = " + String.valueOf(token == null));
            if (token != null){
                Log.d(TAG, "token not null");
                credential = GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(TasksScopes.TASKS));
                credential.setSelectedAccountName(((MainActivity) mActivity).getAccountName());
                service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName("Tasking").build();
                Log.d(TAG, "made the service");
                tasksList = new ArrayList<String>();

                List<Task> tasks = service.tasks().list("@default").execute().getItems();
                Log.d(TAG, "manipulated task");
                for (Task eachTask : tasks){
                    Log.d(TAG, eachTask.toPrettyString());
                }

                Task task = new Task();
                task.setTitle("Poopy");
                task.setNotes("Note notey");
                task.setDue(new DateTime(new Date(2015, 11, 2), TimeZone.getDefault()));
                Task result = service.tasks().insert("@default", task).execute();
                Log.d(TAG, result.toPrettyString());
                // Use token to access user tasks
                //HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

                /*AccessPro accessProtectedResource = new GoogleAccessProtectedResource(token);
                Tasks service = new Tasks(httpTransport, accessProtectedResource, new JacksonFactory());
                service.accessKey = "471356341761-ns1j3gj0h8nq0kf8f34davsa937vhmm0.apps.googleusercontent.com";
                service.setApplicationName("Tasking");*/
                //Tasks service = new Tasks.Builder(httpTransport, new JacksonFactory(), )

   /*             List taskLists = service.tasklists.list().execute().items;

                List tasks = service.tasks.list("@default").execute().items;


                Task task = new Task();
                task.setTitle("titleee");
                task.setNotes("ntoes here");
                task.setDue(new DateTime("2016-10-15T12:00:00.000Z"));
                Task result = service.tasks.insert("@default", task).execute();*/

            }
        } catch (IOException e){
            // The fetchToken() method handles Google-specific exceptions,
            // so there was an exception at a higher level.
            Log.d(TAG, e.toString());
            if (e.getClass() == UserRecoverableAuthIOException.class){
                e = (UserRecoverableAuthIOException) e;
                mActivity.startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), 30);
                Log.d(TAG, "fuck");

                try {
                    List<Task> tasks = service.tasks().list("@default").execute().getItems();
                    Log.d(TAG, "manipulated task");
                    for (Task eachTask : tasks){
                        Log.d(TAG, eachTask.toPrettyString());
                    }

                    Task task = new Task();
                    task.setTitle("Poopy");
                    task.setNotes("Note notey");
                    task.setDue(new DateTime(new Date(2015, 11, 2), TimeZone.getDefault()));
                    Task result = service.tasks().insert("@default", task).execute();
                    Log.d(TAG, result.toPrettyString());
                } catch (IOException x){
                    Log.e(TAG, e.toString());
                }

                // Use token to access user tasks
            }
        }
        return null;
    }

    public interface getAccountName {
        public String getAccountName();
    }

    // Fetches authentication token from Google and
    // handles GoogleAuthExceptions
    protected String fetchToken() throws IOException{
        Log.d(TAG, "fetching token");
        try {
            Log.d(TAG, mActivity.toString() + ", " + mEmail + ", " + mScope);
            Log.d(TAG, "getToken returns: " + String.valueOf(GoogleAuthUtil.getToken(mActivity, mEmail, mScope)));
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
