package me.jakemoritz.tasking;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.List;


public class TaskListFragment extends Fragment implements AbsListView.OnItemClickListener,
        LoadTaskResponse, AddTaskResponse, AbsListView.OnItemLongClickListener,
        ActionMode.Callback, AbsListView.MultiChoiceModeListener{

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

        mAdapter = new TaskAdapter(getActivity(), R.layout.task_item, tasks);

        LoadTasksTask loadTasksTask = new LoadTasksTask(getActivity());
        loadTasksTask.delegate = this;
        loadTasksTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasklist, container, false);

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

    @Override
    public void loadTaskFinish(Object output) {
        tasks = (List<Task>) output;
        mAdapter.clear();
        mAdapter.addAll(tasks);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void addTaskFinish() {
        LoadTasksTask loadTasksTask = new LoadTasksTask(getActivity());
        loadTasksTask.delegate = this;
        loadTasksTask.execute();
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

                for (int i = 0; i < selected.size(); i++){
                    if (selected.valueAt(i)){
                        //delete
                        Task task = mAdapter.getItem(i);
                        mAdapter.remove(task);
                    }
                }
                mode.finish(); // Action picked
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
        mAdapter.toggleSelection(position);
    }
}
