package me.jakemoritz.tasking;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.api.services.tasks.model.Task;

import java.util.List;


public class TaskAdapter extends ArrayAdapter<Task> {

    Context context;
    int layoutResourceId;
    List<Task> taskList = null;

    public TaskAdapter(Context context, int layoutResourceId, List<Task> taskList){
        super(context, layoutResourceId, taskList);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.taskList = taskList;
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

            row.setTag(holder);
        } else {
            holder = (TaskHolder) row.getTag();
        }

        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskNotes.setText(task.getNotes());

        return row;
    }

    static class TaskHolder{
        TextView taskTitle;
        TextView taskNotes;
        TextView taskDate;
        TextView taskTime;
    }
}
