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
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class EditTaskTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "EditTaskTask";

    public EditTaskResponse delegate = null;

    @Override
    protected void onPostExecute(Void aVoid) {
        delegate.editTaskFinish();
    }

    private final static String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    Activity mActivity;
    String mScope;
    String mEmail;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleAccountCredential credential;
    Task task;
    Tasks service;

    public EditTaskTask(Activity mActivity, Task task) {
        this.mActivity = mActivity;

        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("PREFS_ACC", 0);
        this.mEmail = sharedPreferences.getString("email", null);

        this.task = task;
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

                showCurrentDateAndTime(task);

                Task result = service.tasks().update("@default", task.getId(), task).execute();
            }
        } catch (IOException e){
            // The fetchToken() method handles Google-specific exceptions,
            // so there was an exception at a higher level.
            Log.d(TAG, e.toString());
        }
        return null;
    }

    public void showCurrentDateAndTime(Task task){
        DateTime dateTime = task.getDue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateTime.getValue());
        cal.setTimeZone(TimeZone.getDefault());

        int year = cal.get(Calendar.YEAR);
        Log.d(TAG, String.valueOf(year));
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        Date date = new Date(year, month, dayOfMonth);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM");
        dateFormat.setTimeZone(TimeZone.getDefault());

        String[] suffixes =
                //    0     1     2     3     4     5     6     7     8     9
                { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    10    11    12    13    14    15    16    17    18    19
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        //    20    21    22    23    24    25    26    27    28    29
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    30    31
                        "th", "st" };

        String dayString = dayOfMonth + suffixes[dayOfMonth];
        String dateString = dateFormat.format(date) + " " + dayString + ", " + year;
        Log.d(TAG, dateString);
        Time time = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

        String timeString = timeFormat.format(time);

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
