package me.jakemoritz.tasking;

import android.app.Fragment;
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

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.ArrayList;
import java.util.List;


public class TaskListFragment extends Fragment implements AbsListView.OnItemClickListener,
        LoadTasksResponse, AddTaskResponse, AbsListView.OnItemLongClickListener,
        ActionMode.Callback, AbsListView.MultiChoiceModeListener, DeleteTasksResponse,
        EditTaskResponse, SwipeRefreshLayout.OnRefreshListener, RestoreTasksResponse{

    private static final String TAG = "TaskListFragment";

    private AbsListView mListView;

    private TaskAdapter mAdapter;

    List<Task> tasks;

    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasks = new ArrayList<Task>();

        mAdapter = new TaskAdapter(getActivity(), R.layout.task_list_item, tasks);

        LoadTasksTask loadTasksTask = new LoadTasksTask(getActivity());
        loadTasksTask.delegate = this;
        loadTasksTask.execute();
    }

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasklist, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        //swipeRefreshLayout.setColorSchemeColors();

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

    private void createTask(){
        AddTaskDialogFragment addTaskDialogFragment = new AddTaskDialogFragment(this);
        addTaskDialogFragment.show(getFragmentManager(), "addTaskDialog");
    }

    private void editTask(int position){
        Task task = mAdapter.getItem(position);
        EditTaskDialogFragment editTaskDialogFragment = new EditTaskDialogFragment(this, task);
        editTaskDialogFragment.show(getFragmentManager(), "editTaskDialog");
    }

    @Override
    public void loadTasksFinish(Object output) {
        tasks = (List<Task>) output;

        if (tasks != null){
            mAdapter.clear();
            mAdapter.addAll(tasks);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void addTaskFinish() {
        refreshTasks();
    }

    @Override
    public void deleteTasksFinish(final TaskList previousTasks) {
        refreshTasks();
        undoPressed = false;
    }

    @Override
    public void restoreTasksFinish() {
        refreshTasks();
    }


    @Override
    public void editTaskFinish() {
        refreshTasks();
    }

    public void refreshTasks(){
        LoadTasksTask loadTasksTask = new LoadTasksTask(getActivity());
        loadTasksTask.delegate = this;
        loadTasksTask.execute();
    }

    boolean undoPressed = false;



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
        snackbar.setCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);

                if (event == DISMISS_EVENT_TIMEOUT){
                    DeleteTasksTask deleteTasksTask = new DeleteTasksTask(getActivity(), mSelectedIds);
                    deleteTasksTask.delegate = callback;
                    deleteTasksTask.execute();
                }
            }
        });
        snackbar.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
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
        inflater.inflate(R.menu.main, menu);
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
}
