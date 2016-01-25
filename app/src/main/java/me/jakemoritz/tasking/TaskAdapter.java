package me.jakemoritz.tasking;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.Calendar;
import java.util.List;


public class TaskAdapter extends ArrayAdapter<Task>{

    Context context;
    int layoutResourceId;
    List<Task> taskList = null;
    TaskListFragment taskListFragment;
    private SparseBooleanArray mSelectedItemIds;

    public List<Task> getTaskList() {
        return taskList;
    }

    public TaskAdapter(Context context, TaskListFragment taskListFragment, int layoutResourceId, List<Task> taskList){
        super(context, layoutResourceId, taskList);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.taskListFragment = taskListFragment;
        this.taskList = taskList;
        mSelectedItemIds = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TaskHolder holder;

        Task task = taskList.get(position);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new TaskHolder();
        holder.taskTitle = (TextView) row.findViewById(R.id.task_item_title);
        holder.taskNotes = (TextView) row.findViewById(R.id.task_item_notes);
        holder.taskDate = (TextView) row.findViewById(R.id.task_item_date);
        holder.taskCompleted = (CheckBox) row.findViewById(R.id.task_item_checkbox);
        if (task.getStatus().equals(context.getString(R.string.task_completed))){
            holder.taskCompleted.setChecked(true);
        } else {
            holder.taskCompleted.setChecked(false);
        }
        holder.taskCompleted.setOnCheckedChangeListener(taskListFragment);

        row.setTag(holder);

        // Hide title if field empty
        if (task.getTitle() != null){
            holder.taskTitle.setText(task.getTitle());
        } else {
            holder.taskTitle.setVisibility(View.GONE);
        }

        if (task.getNotes() != null){
            holder.taskNotes.setText(task.getNotes());
        } else {
            holder.taskNotes.setVisibility(View.GONE);
        }

        // Get DateTime from task
        DateTime dateTime = null;
        if (task.getStatus().equals(context.getString(R.string.task_completed))){
            dateTime = task.getCompleted();
        } else if (task.getStatus().equals(context.getString(R.string.task_needsAction))){
            dateTime = task.getDue();
        }

        if (dateTime != null){
            long timeInMs = dateTime.getValue();

            // Create calendar from
            Calendar cal = Calendar.getInstance();
            timeInMs -= cal.getTimeZone().getRawOffset(); //fixes UTC time offset in dialog
            cal.setTimeInMillis(timeInMs);

            // Save current date and time values
            int year = cal.get(Calendar.YEAR);
            int monthOfYear = cal.get(Calendar.MONTH);
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

            holder.taskDate.setText(DateFormatter.formatDate(year, monthOfYear, dayOfMonth));
        } else {
            holder.taskDate.setVisibility(View.GONE);
        }

        return row;
    }

    static class TaskHolder{
        TextView taskTitle;
        TextView taskNotes;
        TextView taskDate;
        CheckBox taskCompleted;
    }

    @Override
    public void remove(Task task) {
        taskList.remove(task);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position){
        selectView(position, !mSelectedItemIds.get(position));
    }

    public void removeSelection(){
        mSelectedItemIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value){
        if (value){
            mSelectedItemIds.put(position, value);
        } else {
            mSelectedItemIds.delete(position);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds(){
        return mSelectedItemIds;
    }
}
