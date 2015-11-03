package me.jakemoritz.tasking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.Calendar;


public class EditTaskDialogFragment extends DialogFragment implements TimeSetResponse, DateSetResponse{

    private static final String TAG = "EditTaskDialogFragment";

    final EditTaskDialogFragment callbackInstance = this;

    Fragment parentFragment;
    Task task;

    int year;
    int monthOfYear;
    int dayOfMonth;
    int hourOfDay;
    int minute;
    long timeInMs;

    EditText taskTitle;
    EditText taskNotes;
    TextView chosenDate;
    TextView chosenTime;
    Button datePickerButton;
    Button timePickerButton;

    public EditTaskDialogFragment(Fragment parentFragment, Task task) {
        super();
        this.task = task;
        this.parentFragment = parentFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_task, null);

        chosenDate = (TextView) view.findViewById(R.id.chosen_date);
        chosenTime = (TextView) view.findViewById(R.id.chosen_time);
        taskTitle = (EditText) view.findViewById(R.id.task_title);
        taskNotes = (EditText) view.findViewById(R.id.task_notes);
        datePickerButton = (Button) view.findViewById(R.id.date_picker_button);
        timePickerButton = (Button) view.findViewById(R.id.time_picker_button);

        taskTitle.setText(task.getTitle());
        taskNotes.setText(task.getNotes());
        displayTaskDueDateAndTime();

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

                            // Save time in ms
                            Calendar cal = Calendar.getInstance();
                            cal.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                            timeInMs = cal.getTimeInMillis();

                            DateTime dateTime = new DateTime(timeInMs);

                            task.setDue(dateTime);

                            EditTaskTask editTaskTask = new EditTaskTask(getActivity(), task);
                            editTaskTask.delegate = (TaskListFragment) parentFragment;
                            editTaskTask.execute();

                            dismiss();
                        } else {
                            taskTitle.setError("You must enter a task title.");
                        }
                    }
                });
            }
        });

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment(callbackInstance);
                datePickerFragment.delegate = callbackInstance;
                datePickerFragment.show(getFragmentManager(), "datePickerFragment");
            }
        });


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

    public void displayTaskDueDateAndTime(){
        // Get DateTime from task
        DateTime dateTime = task.getDue();

        // Create calendar from
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateTime.getValue());

        // Save current date and time values
        this.year = cal.get(Calendar.YEAR);
        this.monthOfYear = cal.get(Calendar.MONTH);
        this.dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        this.hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = cal.get(Calendar.MINUTE);

        chosenDate.setText(DateFormatter.formatDate(year, monthOfYear, dayOfMonth));
        chosenTime.setText(TimeFormatter.formatTime(hourOfDay, minute));
    }

    @Override
    public void dateSet(int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;

        chosenDate.setText(DateFormatter.formatDate(year, monthOfYear, dayOfMonth));
    }

    @Override
    public void timeSet(int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;

        chosenTime.setText(TimeFormatter.formatTime(hourOfDay, minute));
    }
}
