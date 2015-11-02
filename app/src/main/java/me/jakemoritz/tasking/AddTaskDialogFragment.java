package me.jakemoritz.tasking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class AddTaskDialogFragment extends DialogFragment implements TimeSetResponse, DateSetResponse{

    private static final String TAG = "AddTaskDialogFragment";

    final AddTaskDialogFragment callbackInstance = this;

    EditText taskTitle;
    EditText taskNotes;
    TextView chosenDate;
    TextView chosenTime;

    Fragment parentFragment;

    public AddTaskDialogFragment(Fragment parentFragment) {
        super();

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(view)
                .setTitle("Create your task.")
                .setPositiveButton("Add", null)
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
                        Log.d(TAG, "text field: " + taskTitle.getText().toString().isEmpty() );
                        if (!taskTitle.getText().toString().isEmpty()){
                            Task task = new Task();
                            task.setTitle(taskTitle.getText().toString());
                            task.setNotes(taskNotes.getText().toString());
                            task.setDue(new DateTime(new Date(year, monthOfYear, dayOfMonth, hourOfDay, minute), TimeZone.getDefault()));
                            List<Task> taskList = new ArrayList<Task>();
                            taskList.add(task);

                            AddTaskTask addTaskTask = new AddTaskTask(getActivity(), taskList);
                            addTaskTask.delegate = (TaskListFragment) parentFragment;
                            addTaskTask.execute();
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

        chosenDate = (TextView) view.findViewById(R.id.chosen_date);

        timePickerButton = (Button) view.findViewById(R.id.time_picker_button);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(callbackInstance);
                timePickerFragment.delegate = callbackInstance;
                timePickerFragment.show(getFragmentManager(), "timePickerFragment");
            }
        });

        chosenTime = (TextView) view.findViewById(R.id.chosen_time);

        taskTitle = (EditText) view.findViewById(R.id.task_title);

        taskNotes = (EditText) view.findViewById(R.id.task_notes);

        displayCurrentDateAndTime();

        return alertDialog;

    }

    public void displayCurrentDateAndTime(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        Date date = new Date(year, month, dayOfMonth);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM");
        dateFormat.setTimeZone(TimeZone.getDefault());

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
        String dateString = dateFormat.format(date) + " " + dayString + ", " + year;

        chosenDate.setText(dateString);

        Time time = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

        String timeString = timeFormat.format(time);
        chosenTime.setText(timeString);
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
