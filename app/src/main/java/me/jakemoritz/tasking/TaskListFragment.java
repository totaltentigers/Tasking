package me.jakemoritz.tasking;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        GetTasksResponse, AddTaskResponse, AbsListView.OnItemLongClickListener,
        ActionMode.Callback, AbsListView.MultiChoiceModeListener, DeleteTasksResponse,
        EditTaskResponse, SwipeRefreshLayout.OnRefreshListener, CheckBox.OnCheckedChangeListener,
        sortTasklistResponse {

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
        addTaskDialogFragment.show(getFragmentManager(), null);
    }

    private void editTask(int position){
        Task task = mAdapter.getItem(position);
        EditTaskDialogFragment editTaskDialogFragment = EditTaskDialogFragment.newInstance(this, task);
        editTaskDialogFragment.show(getFragmentManager(), null);
    }

    @Override
    public void getTasksFinish(List<Task> taskList) {
        if (taskList != null){
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
            saveTasksToDb();
        }
    }

    @Override
    public void addTaskFinish() {
        getTasksFromServer();
    }

    @Override
    public void deleteTasksFinish() {
        getTasksFromServer();
    }

    @Override
    public void editTaskFinish() {
        getTasksFromServer();
    }

    @Override
    public void sortTasklistFinish() {
        getTasksFromServer();
    }

    public void getTasksFromServer(){
        mAdapter.notifyDataSetChanged();

        GetTasksTask getTasksTask = new GetTasksTask(getActivity());
        getTasksTask.delegate = this;
        getTasksTask.execute();
    }

    public void onTasksDeleted(final SparseBooleanArray mSelectedIds, final List<Task> taskList){
        final TaskListFragment callback = this;

        final List<Task> oldTaskList = new ArrayList<>();
        oldTaskList.addAll(taskList);

        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.task_deleted_snackbar_text), Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.task_deleted_snackbar_undo), new View.OnClickListener() {
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
            GetTasksTask getTasksTask = new GetTasksTask(getActivity());
            getTasksTask.delegate = this;
            getTasksTask.execute();
        } else {
            getTasksFromDb();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveTasksToDb();
    }

    public void saveTasksToDb(){
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        Cursor res = null;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 1, 1);

        if (mAdapter.getTaskList() != null){
            for (Task task : mAdapter.getTaskList()){
                if (dbHelper.getTask(task.getId()).getCount() != 0){
                    dbHelper.updateTaskInDb(task.getId(), task);
                } else {
                    dbHelper.insertTask(task);
                }
            }
        }
        dbHelper.close();
    }

    public void getTasksFromDb(){
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

        Cursor res = dbHelper.getAllTasks();

        res.moveToFirst();
        List<Task> taskList = new ArrayList<>();

        for (int i = res.getCount() - 1; i >= 0; i--){
            String taskId = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_ID));
            String taskTitle = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_TITLE));
            String taskNotes = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_NOTES));
            String taskStatus = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_STATUS));
            String taskDueDate = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_DUE_DATE));
            String taskCompletedDate = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_COMP_DATE));

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

            SortTasklistTask sortTasklistTask = new SortTasklistTask(getActivity(), mAdapter.getTaskList());
            sortTasklistTask.delegate = this;
            sortTasklistTask.execute();
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

                List<Task> tasksToDelete = new ArrayList<>();
                for (int i = 0; i < selected.size(); i++){
                    tasksToDelete.add(mAdapter.getItem(selected.keyAt(i)));
                }

                List<Task> tasksToKeep = new ArrayList<>();
                for (Task task : mAdapter.getTaskList()){
                    boolean mustKeep = true;
                    for (Task taskToDelete : tasksToDelete){
                        if (task == taskToDelete){
                            mustKeep = false;
                        }
                    }
                    if (mustKeep){
                        tasksToKeep.add(task);
                    }
                    mustKeep = true;
                }

                onTasksDeleted(selected, mAdapter.getTaskList());

                mAdapter.clear();
                mAdapter.addAll(tasksToKeep);
                mAdapter.notifyDataSetChanged();
                saveTasksToDb();

//                for (int i = 0; i < selected.size(); i++){
//                    if (selected.valueAt(i)){
//                        Task task = mAdapter.getItem(selected.keyAt(i));
//                        mAdapter.remove(task);
//                    }
//                }
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
        getTasksFromServer();
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
            task.setStatus(getString(R.string.task_completed));
            task.setCompleted(task.getDue());
            newTask.setStatus(getString(R.string.task_completed));
            newTask.setDue(task.getDue());
            newTask.setCompleted(task.getDue());
        } else {
            task.setStatus(getString(R.string.task_needsAction));
            task.setDue(task.getCompleted());
            newTask.setStatus(getString(R.string.task_needsAction));
            newTask.setDue(task.getCompleted());
        }

        EditTaskTask editTaskTask = new EditTaskTask(getActivity(), newTask);
        editTaskTask.delegate = this;
        editTaskTask.execute();
    }
}
