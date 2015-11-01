package me.jakemoritz.tasking;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.v1.Tasks;

import java.io.IOException;
import java.util.List;

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

    // Executes asynchronous job.
    // Runs when you call execute() on an instance
    @Override
    protected Void doInBackground(Void... params) {
        try {
            String token = fetchToken();
            if (token != null){
                // Use token to access user tasks
                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                com.google.api.client.auth.oauth2.draft10.AccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(token);
                Tasks service = new Tasks(httpTransport, accessProtectedResource, new JacksonFactory());
                service.accessKey = "471356341761-ns1j3gj0h8nq0kf8f34davsa937vhmm0.apps.googleusercontent.com";
                service.setApplicationName("Tasking");

                List taskLists = service.tasklists.list().execute().items;

                List tasks = service.tasks.list("@default").execute().items;


                Task task = new Task();
                task.setTitle("titleee");
                task.setNotes("ntoes here");
                task.setDue(new DateTime("2016-10-15T12:00:00.000Z"));
                Task result = service.tasks.insert("@default", task).execute();

            }
        } catch (IOException e){
            // The fetchToken() method handles Google-specific exceptions,
            // so there was an exception at a higher level.
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
        } catch (GoogleAuthException fatalException){
            Log.e(TAG, fatalException.toString());
        }
        return null;
    }
}
