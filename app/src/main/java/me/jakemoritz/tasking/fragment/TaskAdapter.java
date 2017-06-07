package me.jakemoritz.tasking.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.List;

import me.jakemoritz.tasking.R;
import me.jakemoritz.tasking.helper.DateFormatter;


class TaskAdapter extends ArrayAdapter<Task>{

    private Context context;
    private int taskItemLayoutId;
    private List<Task> taskList = null;
    private TaskListFragment taskListFragment;
    private SparseBooleanArray mSelectedItemIds;

    List<Task> getTaskList() {
        return taskList;
    }

    TaskAdapter(Context context, TaskListFragment taskListFragment, int taskItemLayoutId, List<Task> taskList){
        super(context, taskItemLayoutId, taskList);
        this.taskItemLayoutId = taskItemLayoutId;
        this.context = context;
        this.taskListFragment = taskListFragment;
        this.taskList = taskList;
        this.mSelectedItemIds = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Task task = taskList.get(position);

        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(taskItemLayoutId, parent, false);
        }

        // Initialize row views
        TextView taskTitle = (TextView) convertView.findViewById(R.id.task_item_title);
        TextView taskNotes = (TextView) convertView.findViewById(R.id.task_item_notes);
        TextView taskDate = (TextView) convertView.findViewById(R.id.task_item_date);
        CheckBox taskCompleted = (CheckBox) convertView.findViewById(R.id.task_item_checkbox);
        CardView taskCardView = (CardView) convertView.findViewById(R.id.card);

        // Update views with task values
        if (task.getStatus().equals(context.getString(R.string.task_completed))){
            taskCompleted.setChecked(true);
        } else {
            taskCompleted.setChecked(false);
        }

        // Highlight row if selected
        for (int i = 0; i < mSelectedItemIds.size(); i++){
            if (position == mSelectedItemIds.keyAt(i)){
                taskCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            }
        }

        taskCompleted.setOnCheckedChangeListener(taskListFragment);

        // Hide title if field empty
        if (task.getTitle() != null){
            taskTitle.setText(task.getTitle());
        } else {
            taskTitle.setVisibility(View.GONE);
        }

        // Hide notes if field empty
        if (task.getNotes() != null){
            taskNotes.setText(task.getNotes());
        } else {
            taskNotes.setVisibility(View.GONE);
        }

        // Get DateTime from task
        DateTime taskDateTime = null;
        if (task.getStatus().equals(context.getString(R.string.task_completed))){
            taskDateTime = task.getCompleted();
        } else if (task.getStatus().equals(context.getString(R.string.task_needsAction))){
            taskDateTime = task.getDue();
        }

        if (taskDateTime != null){
            taskDate.setText(DateFormatter.getInstance().formatDate(taskDateTime));
        } else {
            taskDate.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public void remove(Task task) {
        taskList.remove(task);
        notifyDataSetChanged();
    }

    void toggleSelection(int position){
        selectView(position, !mSelectedItemIds.get(position));
    }

    void removeSelection(){
        mSelectedItemIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    private void selectView(int position, boolean value){
        if (value){
            mSelectedItemIds.put(position, true);
        } else {
            mSelectedItemIds.delete(position);
        }
        notifyDataSetChanged();
    }

    SparseBooleanArray getSelectedIds(){
        return mSelectedItemIds;
    }
}
