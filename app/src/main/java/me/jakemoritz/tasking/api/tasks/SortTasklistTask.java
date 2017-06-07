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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.tasking.R;
import me.jakemoritz.tasking.helper.SharedPrefsHelper;

public class SortTasklistTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = SortTasklistTask.class.getSimpleName();

    private SortTasklistResponse delegate = null;
    private Activity mActivity;
    private String mEmail;
    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private List<Task> taskList;

    public SortTasklistTask(Activity mActivity, SortTasklistResponse delegate, List<Task> taskList) {
        this.mActivity = mActivity;
        this.mEmail = SharedPrefsHelper.getInstance().getUserEmail();
        this.taskList = taskList;
        this.delegate = delegate;
    }

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

            // Reverses order of task list
            List<Task> reversedList = new ArrayList<>();
            reversedList.addAll(taskList);
            Collections.reverse(reversedList);

            // Iterates through reversed list, moving each task to its new position
            for (Task task : reversedList) {
                service.tasks().move(firstTaskListId, task.getId()).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        delegate.tasksSorted();
    }
}
