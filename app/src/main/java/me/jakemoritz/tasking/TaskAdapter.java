package me.jakemoritz.tasking;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.Calendar;
import java.util.List;


public class TaskAdapter extends ArrayAdapter<Task> {

    Context context;
    int layoutResourceId;

    public List<Task> getTaskList() {
        return taskList;
    }

    List<Task> taskList = null;
    private SparseBooleanArray mSelectedItemIds;

    @Override
    public void remove(Task object) {
        taskList.remove(object);
        notifyDataSetChanged();
    }

    public TaskAdapter(Context context, int layoutResourceId, List<Task> taskList){
        super(context, layoutResourceId, taskList);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.taskList = taskList;
        mSelectedItemIds = new SparseBooleanArray();
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

    public int getSelectedCount(){
        return mSelectedItemIds.size();
    }

    public SparseBooleanArray getSelectedIds(){
        return mSelectedItemIds;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TaskHolder holder = null;

        if (row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TaskHolder();
            holder.taskTitle = (TextView) row.findViewById(R.id.task_item_title);
            holder.taskNotes = (TextView) row.findViewById(R.id.task_item_notes);
            holder.taskDate = (TextView) row.findViewById(R.id.task_item_date);
            //holder.taskTime = (TextView) row.findViewById(R.id.task_item_time);

            row.setTag(holder);
        } else {
            holder = (TaskHolder) row.getTag();
        }

        Task task = taskList.get(position);

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
        DateTime dateTime = task.getDue();


        if (dateTime != null){
            // Create calendar from
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dateTime.getValue());

            // Save current date and time values
            int year = cal.get(Calendar.YEAR);
            int monthOfYear = cal.get(Calendar.MONTH);
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            //int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            //int minute = cal.get(Calendar.MINUTE);

            holder.taskDate.setText(DateFormatter.formatDate(year, monthOfYear, dayOfMonth));
            //holder.taskTime.setText(TimeFormatter.formatTime(hourOfDay, minute));
        } else {
            holder.taskNotes.setVisibility(View.GONE);
        }

        return row;
    }

    static class TaskHolder{
        TextView taskTitle;
        TextView taskNotes;
        TextView taskDate;
        //TextView taskTime;
    }
}
