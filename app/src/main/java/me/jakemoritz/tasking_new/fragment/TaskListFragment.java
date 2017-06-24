package me.jakemoritz.tasking_new.fragment;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jakemoritz.tasking_new.R;
import me.jakemoritz.tasking_new.activity.MainActivity;
import me.jakemoritz.tasking_new.api.tasks.AddTaskResponse;
import me.jakemoritz.tasking_new.api.tasks.DeleteTasksResponse;
import me.jakemoritz.tasking_new.api.tasks.DeleteTasksTask;
import me.jakemoritz.tasking_new.api.tasks.EditTaskResponse;
import me.jakemoritz.tasking_new.api.tasks.EditTaskTask;
import me.jakemoritz.tasking_new.api.tasks.GetTasksResponse;
import me.jakemoritz.tasking_new.api.tasks.GetTasksTask;
import me.jakemoritz.tasking_new.api.tasks.SortTasklistResponse;
import me.jakemoritz.tasking_new.api.tasks.SortTasklistTask;
import me.jakemoritz.tasking_new.database.DatabaseHelper;
import me.jakemoritz.tasking_new.dialog.AddTaskDialogFragment;
import me.jakemoritz.tasking_new.dialog.EditTaskDialogFragment;
import me.jakemoritz.tasking_new.misc.App;
import me.jakemoritz.tasking_new.misc.CompareTaskDueDate;
import me.jakemoritz.tasking_new.activity.MainActivity.PermissionRequired;


public class TaskListFragment extends ListFragment implements GetTasksResponse, AddTaskResponse,
        DeleteTasksResponse, EditTaskResponse, SwipeRefreshLayout.OnRefreshListener, CheckBox.OnCheckedChangeListener,
        SortTasklistResponse {

    private static final String TAG = TaskListFragment.class.getSimpleName();

    private MainActivity mainActivity;
    private AddLaunched addLaunched;

    // Views
    private AbsListView mListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    private TaskAdapter mAdapter;

    // Data
    private List<Task> tasks;

    public TaskListFragment() {
    }

    public static TaskListFragment newInstance(){
        TaskListFragment taskListFragment = new TaskListFragment();
        taskListFragment.setRetainInstance(true);
        taskListFragment.setHasOptionsMenu(true);
        return taskListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        tasks = new ArrayList<>();
        mAdapter = new TaskAdapter(getActivity(), this, R.layout.task_list_item, tasks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasklist, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setEmptyView(getActivity().findViewById(android.R.id.empty));
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setLongClickable(true);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = mListView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
                mAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        SparseBooleanArray selected = mAdapter.getSelectedIds();

                        List<Task> tasksToDelete = new ArrayList<>();
                        for (int i = 0; i < selected.size(); i++) {
                            tasksToDelete.add(mAdapter.getItem(selected.keyAt(i)));
                        }

                        List<Task> tasksToKeep = new ArrayList<>();
                        for (Task task : mAdapter.getTaskList()) {
                            boolean mustKeep = true;
                            for (Task taskToDelete : tasksToDelete) {
                                if (task == taskToDelete) {
                                    mustKeep = false;
                                }
                            }
                            if (mustKeep) {
                                tasksToKeep.add(task);
                            }
                        }

                        onTasksDeleted(selected, mAdapter.getTaskList());

                        mAdapter.clear();
                        mAdapter.addAll(tasksToKeep);
                        mAdapter.notifyDataSetChanged();

                        DatabaseHelper databaseHelper = new DatabaseHelper(App.getInstance());
                        databaseHelper.saveTasksToDb(mAdapter.getTaskList());
                        databaseHelper.close();

                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mAdapter.removeSelection();
            }
        });

        // Display active progress bar
        progressBar = (ProgressBar)view.findViewById(R.id.task_load_progress);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        // Initialize new task FAB
        FloatingActionButton newTaskFab = (FloatingActionButton) view.findViewById(R.id.fab);
        newTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddTaskDialog();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Checks internet availability to choose task data source
        if (App.getInstance().isNetworkAvailable()) {
            GetTasksTask getTasksTask = new GetTasksTask(getActivity(), this);
            getTasksTask.execute();
        } else {
            DatabaseHelper databaseHelper = new DatabaseHelper(App.getInstance());
            List<Task> tasks = databaseHelper.getTasksFromDb();
            databaseHelper.close();

            if (!tasks.isEmpty()) {
                mAdapter.clear();
                mAdapter.addAll(tasks);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        DatabaseHelper databaseHelper = new DatabaseHelper(App.getInstance());
        databaseHelper.saveTasksToDb(mAdapter.getTaskList());
        databaseHelper.close();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mainActivity.getSupportActionBar() != null){
            mainActivity.getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mainActivity = (MainActivity) context;
        this.addLaunched = this.mainActivity;
    }

    @Override
    public void onRefresh() {
        getTasksFromServer();
        swipeRefreshLayout.setRefreshing(true);
    }

    // Task CRUD methods
    private void openAddTaskDialog() {
        AddTaskDialogFragment addTaskDialogFragment = AddTaskDialogFragment.newInstance(this);
        addTaskDialogFragment.show(getFragmentManager(), AddTaskDialogFragment.class.getSimpleName());

        this.addLaunched.addLaunched(addTaskDialogFragment);
    }

    public interface AddLaunched{
        void addLaunched(PermissionRequired permissionRequired);
    }

    private void editTask(int position) {
        Task task = mAdapter.getItem(position);
        EditTaskDialogFragment editTaskDialogFragment = EditTaskDialogFragment.newInstance(this, task);
        editTaskDialogFragment.show(getFragmentManager(), EditTaskDialogFragment.class.getSimpleName());
    }

    private void getTasksFromServer() {
        mAdapter.notifyDataSetChanged();

        GetTasksTask getTasksTask = new GetTasksTask(getActivity(), this);
        getTasksTask.execute();
    }

    private void sortTasks() {
        List<Task> taskList = new ArrayList<>();
        taskList.addAll(mAdapter.getTaskList());

        if (!taskList.isEmpty()) {
            Collections.sort(taskList, new CompareTaskDueDate());

            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();

            SortTasklistTask sortTasklistTask = new SortTasklistTask(getActivity(), this, mAdapter.getTaskList());
            sortTasklistTask.execute();
        }
    }

    // Task AsyncTask callbacks
    @Override
    public void tasksReceived(List<Task> taskList) {
        if (taskList != null) {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();

            DatabaseHelper databaseHelper = new DatabaseHelper(App.getInstance());
            databaseHelper.saveTasksToDb(mAdapter.getTaskList());
            databaseHelper.close();

            // Disable updating views
            progressBar.setVisibility(View.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void taskAdded() {
        getTasksFromServer();
    }

    @Override
    public void tasksDeleted() {
        getTasksFromServer();
    }

    @Override
    public void taskEdited() {
        getTasksFromServer();
    }

    @Override
    public void tasksSorted() {
        getTasksFromServer();
    }

    // Handles batch task deletion
    private void onTasksDeleted(final SparseBooleanArray mSelectedIds, final List<Task> taskList) {
        final TaskListFragment callback = this;

        final List<Task> oldTaskList = new ArrayList<>();
        oldTaskList.addAll(taskList);

        if (getView() != null){
            Snackbar snackbar = Snackbar.make(getView(), getString(R.string.task_deleted_snackbar_text), Snackbar.LENGTH_LONG);
            snackbar.setAction(getString(R.string.task_deleted_snackbar_undo), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.clear();
                    mAdapter.addAll(oldTaskList);
                    mAdapter.notifyDataSetChanged();
                }
            });

            // Adds Snackbar dismiss callback to allow user to undo task deletion
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);

                    if (event == DISMISS_EVENT_TIMEOUT) {
                        DeleteTasksTask deleteTasksTask = new DeleteTasksTask(getActivity(), callback, mSelectedIds);
                        deleteTasksTask.execute();
                    }
                }
            });
            snackbar.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            PopupMenu popupMenu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.action_sort));
            popupMenu.inflate(R.menu.sort_popup_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sortTasks();
                    return true;
                }
            });
            popupMenu.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        editTask(position);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int position = mListView.getPositionForView(buttonView);
        Task task = tasks.get(position);
        Task newTask = new Task();
        newTask.setId(task.getId());
        newTask.setTitle(task.getTitle());
        newTask.setNotes(task.getNotes());

        if (isChecked) {
            task.setStatus(getString(R.string.task_completed));
            task.setCompleted(task.getDue());
            newTask.setStatus(getString(R.string.task_completed));
            newTask.setDue(task.getDue());
            newTask.setCompleted(task.getDue());
        } else {
            task.setStatus(getString(R.string.task_needs_action));
            task.setDue(task.getCompleted());
            newTask.setStatus(getString(R.string.task_needs_action));
            newTask.setDue(task.getCompleted());
        }

        EditTaskTask editTaskTask = new EditTaskTask(getActivity(), this, newTask);
        editTaskTask.execute();
    }
}
