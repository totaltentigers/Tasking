package me.jakemoritz.tasking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class EditTaskDialogFragment extends DialogFragment implements TimeSetResponse, DateSetResponse{

    private static final String TAG = "EditTaskDialogFragment";

    final EditTaskDialogFragment callbackInstance = this;

    EditText taskTitle;
    EditText taskNotes;
    TextView chosenDate;
    TextView chosenTime;

    Fragment parentFragment;
    Task task;

    public EditTaskDialogFragment(Fragment parentFragment, Task task) {
        super();
        this.task = task;
        this.parentFragment = parentFragment;

        Log.d(TAG, this.parentFragment.toString());

    }

    Button datePickerButton;
    Button timePickerButton;

    int year;
    int monthOfYear;
    int dayOfMonth;
    int hourOfDay;
    int minute;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_task, null);

        chosenDate = (TextView) view.findViewById(R.id.chosen_date);
        chosenTime = (TextView) view.findViewById(R.id.chosen_time);
        taskTitle = (EditText) view.findViewById(R.id.task_title);
        taskNotes = (EditText) view.findViewById(R.id.task_notes);

        taskTitle.setText(task.getTitle());
        taskNotes.setText(task.getNotes());
        chosenDate.setText(task.getDue().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(view)
                .setTitle("Edit your task.")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!taskTitle.getText().toString().isEmpty()) {
                            task.setTitle(taskTitle.getText().toString());
                            task.setNotes(taskNotes.getText().toString());
                            task.setDue(new DateTime(new Date(year, monthOfYear, dayOfMonth, hourOfDay, minute), TimeZone.getDefault()));
                            List<Task> taskList = new ArrayList<Task>();
                            taskList.add(task);

                            EditTaskTask editTaskTask = new EditTaskTask(getActivity(), task);
                            editTaskTask.delegate = (TaskListFragment) parentFragment;
                            editTaskTask.execute();
                        } else {
                            taskTitle.setError("You must enter a task title.");

                        }

                    }
                });
            }
        });

        datePickerButton = (Button) view.findViewById(R.id.date_picker_button);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment(callbackInstance);
                datePickerFragment.delegate = callbackInstance;
                datePickerFragment.show(getFragmentManager(), "datePickerFragment");
            }
        });


        timePickerButton = (Button) view.findViewById(R.id.time_picker_button);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(callbackInstance);
                timePickerFragment.delegate = callbackInstance;
                timePickerFragment.show(getFragmentManager(), "timePickerFragment");
            }
        });


        return alertDialog;

    }

    @Override
    public void dateSet(int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;

        SimpleDateFormat format = new SimpleDateFormat("MMMM");
        format.setTimeZone(TimeZone.getDefault());
        Date date = new Date(year, monthOfYear, dayOfMonth);

        String[] suffixes =
                //    0     1     2     3     4     5     6     7     8     9
                { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    10    11    12    13    14    15    16    17    18    19
                        "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                        //    20    21    22    23    24    25    26    27    28    29
                        "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                        //    30    31
                        "th", "st" };

        String dayString = dayOfMonth + suffixes[dayOfMonth];
        String dateString = format.format(date) + " " + dayString + ", " + year;

        chosenDate.setText(dateString);
    }

    @Override
    public void timeSet(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;

        Time time = new Time(hourOfDay, minute, 0);
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");

        String timeString = format.format(time);
        chosenTime.setText(timeString);
    }
}
