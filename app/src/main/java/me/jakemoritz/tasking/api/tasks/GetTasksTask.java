package me.jakemoritz.tasking.api.tasks;

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

import me.jakemoritz.tasking.R;
import me.jakemoritz.tasking.helper.SharedPrefsHelper;

public class GetTasksTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = GetTasksTask.class.getSimpleName();

    private GetTasksResponse delegate = null;
    private Activity mActivity;
    private String mEmail;
    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private List<Task> tasks;

    public GetTasksTask(Activity mActivity, GetTasksResponse delegate) {
        this.mActivity = mActivity;
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

            // Gets tasks from default task list
            tasks = service.tasks().list(firstTaskListId).execute().getItems();

            if (tasks != null) {
                // Removes empty task
                Task emptyTask = tasks.get(tasks.size() - 1);
                if (emptyTask.getTitle().length() == 0 && emptyTask.getNotes() == null) {
                    tasks.remove(tasks.size() - 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        delegate.tasksReceived(tasks);
    }
}
