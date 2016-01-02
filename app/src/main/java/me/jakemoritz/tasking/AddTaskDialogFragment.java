package me.jakemoritz.tasking;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.Calendar;
import java.util.TimeZone;


public class AddTaskDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    private static final String TAG = "AddTaskDialogFragment";

    final AddTaskDialogFragment callbackInstance = this;

    Fragment parentFragment;

    int year;
    int monthOfYear;
    int dayOfMonth;
    long timeInMs;

    EditText taskTitle;
    EditText taskNotes;
    TextView chosenDate;
    Button datePickerButton;

    public static AddTaskDialogFragment newInstance(Fragment parentFragment) {
        AddTaskDialogFragment addTaskDialogFragment = new AddTaskDialogFragment();
        addTaskDialogFragment.parentFragment = parentFragment;
        return addTaskDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_task, null);

        chosenDate = (TextView) view.findViewById(R.id.chosen_date);
        taskTitle = (EditText) view.findViewById(R.id.task_title);
        taskNotes = (EditText) view.findViewById(R.id.task_notes);
        datePickerButton = (Button) view.findViewById(R.id.date_picker_button);

        displayCurrentDate();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(getString(R.string.add_task_dialog_title))
                .setPositiveButton(getString(R.string.add_task_dialog_add), null)
                .setNegativeButton(getString(R.string.add_task_dialog_cancel), new DialogInterface.OnClickListener() {
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
                            Task task = new Task();
                            task.setTitle(taskTitle.getText().toString());
                            task.setNotes(taskNotes.getText().toString());

                            // Save time in ms
                            Calendar cal = Calendar.getInstance();
                            cal.set(year, monthOfYear, dayOfMonth);
                            cal.setTimeZone(TimeZone.getDefault());
                            timeInMs = cal.getTimeInMillis();

                            DateTime dateTime = new DateTime(timeInMs);
                            task.setDue(dateTime);

                            AddTaskTask addTaskTask = new AddTaskTask(getActivity(), task);
                            addTaskTask.delegate = (TaskListFragment) parentFragment;
                            addTaskTask.execute();
                            dismiss();
                        } else {
                            taskTitle.setError(getString(R.string.add_task_dialog_error_notitle));
                        }
                    }
                });
            }
        });

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(callbackInstance);
                datePickerFragment.show(getFragmentManager(), null);
            }
        });

        return alertDialog;
    }

    public void displayCurrentDate(){
        // Get new Calendar instance
        Calendar cal = Calendar.getInstance();

        // Save current date and time values
        this.year = cal.get(Calendar.YEAR);
        this.monthOfYear = cal.get(Calendar.MONTH);
        this.dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        chosenDate.setText(DateFormatter.formatDate(year, monthOfYear, dayOfMonth));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;

        chosenDate.setText(DateFormatter.formatDate(year, monthOfYear, dayOfMonth));
    }
}
