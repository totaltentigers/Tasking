package me.jakemoritz.tasking;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupMenu;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TaskListFragment extends Fragment implements AbsListView.OnItemClickListener,
        LoadTasksResponse, AddTaskResponse, AbsListView.OnItemLongClickListener,
        ActionMode.Callback, AbsListView.MultiChoiceModeListener, DeleteTasksResponse,
        EditTaskResponse, SwipeRefreshLayout.OnRefreshListener, CheckBox.OnCheckedChangeListener,
        UpdateTasklistResponse{

    private static final String TAG = "TaskListFragment";

    private AbsListView mListView;
    SwipeRefreshLayout swipeRefreshLayout;

    private TaskAdapter mAdapter;

    List<Task> tasks;

    public TaskListFragment() {
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
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setLongClickable(true);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    private void createTask(){
        AddTaskDialogFragment addTaskDialogFragment = AddTaskDialogFragment.newInstance(this);
        addTaskDialogFragment.show(getFragmentManager(), "addTaskDialog");
    }

    private void editTask(int position){
        Task task = mAdapter.getItem(position);
        EditTaskDialogFragment editTaskDialogFragment = EditTaskDialogFragment.newInstance(this, task);
        editTaskDialogFragment.show(getFragmentManager(), "editTaskDialog");
    }

    @Override
    public void loadTasksFinish(List<Task> taskList) {
        if (taskList != null){
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
            saveUserTasks();
        }
    }

    @Override
    public void addTaskFinish() {
        refreshTasks();
    }

    @Override
    public void deleteTasksFinish() {
        refreshTasks();
    }

    @Override
    public void editTaskFinish() {
        refreshTasks();
    }

    @Override
    public void updateTasklistFinish() {
        refreshTasks();
    }

    public void refreshTasks(){
        mAdapter.notifyDataSetChanged();

        LoadTasksTask loadTasksTask = new LoadTasksTask(getActivity());
        loadTasksTask.delegate = this;
        loadTasksTask.execute();
    }

    public void onTasksDeleted(final SparseBooleanArray mSelectedIds, final List<Task> taskList){
        final TaskListFragment callback = this;

        final List<Task> oldTaskList = new ArrayList<>();
        oldTaskList.addAll(taskList);

        Snackbar snackbar = Snackbar.make(getView(), "Deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.clear();
                mAdapter.addAll(oldTaskList);
                mAdapter.notifyDataSetChanged();
            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);

                if (event == DISMISS_EVENT_TIMEOUT) {
                    DeleteTasksTask deleteTasksTask = new DeleteTasksTask(getActivity(), mSelectedIds);
                    deleteTasksTask.delegate = callback;
                    deleteTasksTask.execute();
                }
            }
        });
        snackbar.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isNetworkAvailable()){
            LoadTasksTask loadTasksTask = new LoadTasksTask(getActivity());
            loadTasksTask.delegate = this;
            loadTasksTask.execute();
        } else {
            loadUserTasks();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveUserTasks();
    }

    public void saveUserTasks(){
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        Cursor res = null;

        if (mAdapter.getTaskList() != null){
            for (Task task : mAdapter.getTaskList()){
                if (dbHelper.getTask(task.getId()).getCount() != 0){
                    dbHelper.updateTask(task.getId(), task);
                } else {
                    dbHelper.insertTask(task);
                }
            }
        }
        dbHelper.close();
    }

    public void loadUserTasks(){
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

        Cursor res = dbHelper.getAllTasks();

        res.moveToFirst();
        List<Task> taskList = new ArrayList<>();

        for (int i = res.getCount() - 1; i >= 0; i--){
            String taskId = res.getString(res.getColumnIndex("_id"));
            String taskTitle = res.getString(res.getColumnIndex("title"));
            String taskNotes = res.getString(res.getColumnIndex("notes"));
            String taskStatus = res.getString(res.getColumnIndex("status"));
            String taskDueDate = res.getString(res.getColumnIndex("duedate"));
            String taskCompletedDate = res.getString(res.getColumnIndex("completeddate"));

            Task task = new Task();
            task.setId(taskId);
            if (taskTitle != null){
                task.setTitle(taskTitle);
            }
            if (taskNotes != null){
                task.setNotes(taskNotes);
            }
            if (taskStatus != null){
                task.setStatus(taskStatus);
            }
            if (taskDueDate != null){
                task.setDue(new DateTime(Long.valueOf(taskDueDate)));
            }
            if (taskCompletedDate != null){
                task.setCompleted(new DateTime(Long.valueOf(taskCompletedDate)));
            }
            taskList.add(task);
            res.moveToNext();
        }

        if (!taskList.isEmpty()){
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }
        dbHelper.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    public void sortTasks(){
        List<Task> taskList = new ArrayList<>();
        taskList.addAll(mAdapter.getTaskList());

        if (!taskList.isEmpty()){
            Collections.sort(taskList, new CompareTaskDueDate());

            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();

            UpdateTasklistTask updateTasklistTask = new UpdateTasklistTask(getActivity(), mAdapter.getTaskList());
            updateTasklistTask.delegate = this;
            updateTasklistTask.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sort) {
//            View actionSortView = getView().findViewById(R.id.)
            PopupMenu popupMenu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.action_sort));
            popupMenu.inflate(R.menu.sort_popup_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sortTasks();
                    return false;
                }
            });
            popupMenu.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editTask(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        getActivity().startActionMode(this);
        mListView.setItemChecked(position, true);
        return true;
    }

    // Called when action mode is created; startActionMode() called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
        return true;
    }

    // Called when action mode is shown; always after onCreateActionMode or invalidated
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    // Called when user selects contextual menu item
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                SparseBooleanArray selected = mAdapter.getSelectedIds();

                onTasksDeleted(selected, mAdapter.getTaskList());

                for (int i = 0; i < selected.size(); i++){
                    if (selected.valueAt(i)){
                        Task task = mAdapter.getItem(selected.keyAt(i));
                        mAdapter.remove(task);
                    }
                }
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    // Called when user exits action mode
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mAdapter.removeSelection();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        final int checkedCount = mListView.getCheckedItemCount();
        mode.setTitle(checkedCount + " Selected");
        mAdapter.toggleSelection(position);
    }

    @Override
    public void onRefresh() {
        refreshTasks();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int position = mListView.getPositionForView(buttonView);
        Task task = tasks.get(position);
        Task newTask = new Task();
        newTask.setId(task.getId());
        newTask.setTitle(task.getTitle());
        newTask.setNotes(task.getNotes());
        if (isChecked){
            task.setStatus("completed");
            task.setCompleted(task.getDue());
            newTask.setStatus("completed");
            newTask.setDue(task.getDue());
            newTask.setCompleted(task.getDue());
        } else {
            task.setStatus("needsAction");
            task.setDue(task.getCompleted());
            newTask.setStatus("needsAction");
            newTask.setDue(task.getCompleted());
        }

        EditTaskTask editTaskTask = new EditTaskTask(getActivity(), newTask);
        editTaskTask.delegate = this;
        editTaskTask.execute();
    }
}
