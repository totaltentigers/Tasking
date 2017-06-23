package me.jakemoritz.tasking_new.api.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.SparseBooleanArray;

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

public class DeleteTasksTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = DeleteTasksTask.class.getSimpleName();

    private DeleteTasksResponse delegate = null;
    private Activity mActivity;
    private String mEmail;
    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private SparseBooleanArray mSelectedItemIds;

    public DeleteTasksTask(Activity mActivity, DeleteTasksResponse delegate, SparseBooleanArray mSelectedItemIds) {
        this.mActivity = mActivity;
        this.mSelectedItemIds = mSelectedItemIds;
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

            // Gets tasks from default list
            List<Task> tasks = service.tasks().list(firstTaskListId).execute().getItems();

            DatabaseHelper dbHelper = new DatabaseHelper(mActivity);

            // Loop through selected tasks, deleting each from default list
            for (int i = 0; i < mSelectedItemIds.size(); i++) {
                Task task = tasks.get(mSelectedItemIds.keyAt(i));
                dbHelper.deleteTask(task.getId());
                service.tasks().delete(firstTaskListId, task.getId()).execute();
            }

            dbHelper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        delegate.tasksDeleted();
    }
}
