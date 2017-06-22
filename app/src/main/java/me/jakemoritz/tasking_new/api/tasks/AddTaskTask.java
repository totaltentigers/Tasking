package me.jakemoritz.tasking_new.api.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.tasking_new.R;
import me.jakemoritz.tasking_new.database.DatabaseHelper;
import me.jakemoritz.tasking_new.helper.SharedPrefsHelper;

public class AddTaskTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = AddTaskTask.class.getSimpleName();

    private AddTaskResponse delegate = null;
    private Activity mActivity;
    private String mEmail;
    private Task task;
    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    public AddTaskTask(Activity mActivity, AddTaskResponse delegate, Task task) {
        this.mActivity = mActivity;
        this.task = task;
        this.mEmail = SharedPrefsHelper.getInstance().getUserEmail();
        this.delegate = delegate;
    }

    // Executes asynchronous job.
    // Runs when you call execute() on an instance
    @Override
    protected Void doInBackground(Void... params) {
        try {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mActivity, Collections.singleton(TasksScopes.TASKS));
            credential.setSelectedAccountName(mEmail);

            // Build Tasks service object
            Tasks service = new Tasks.Builder(httpTransport, jsonFactory, credential).setApplicationName(mActivity.getString(R.string.app_name)).build();

            // Gets list of user's task lists
            List<TaskList> taskLists = service.tasklists().list().execute().getItems();

            // Gets default list
            String firstTaskListId = taskLists.get(0).getId();

            // Insert task into default list
            service.tasks().insert(firstTaskListId, task).execute();

            // Save new task to database
            DatabaseHelper dbHelper = new DatabaseHelper(mActivity);
            dbHelper.insertTask(task);
            dbHelper.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (delegate != null) {
            delegate.taskAdded();
        }
    }
}
