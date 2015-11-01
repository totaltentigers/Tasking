package me.jakemoritz.tasking;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class AddTaskDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{

    private static final String TAG = "AddTaskDialogFragment";

    final AddTaskDialogFragment callbackInstance = this;

    EditText taskTitle;
    EditText taskNotes;
    TextView chosenDate;
    TextView chosenTime;
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
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Task task = new Task();
                        task.setTitle(taskTitle.getText().toString());
                        task.setNotes(taskNotes.getText().toString());
                        task.setDue(new DateTime(new Date(year, monthOfYear, dayOfMonth, hourOfDay, minute), TimeZone.getDefault()));
                        List<Task> taskList = new ArrayList<Task>();
                        taskList.add(task);

                        // Check sign-in state
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("PREFS_ACC", 0);
                        String mEmail = sharedPreferences.getString("email", null);

                        AddTaskTask aaaa = new AddTaskTask(getActivity(), mEmail, taskList);
                        aaaa.execute();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();

        datePickerButton = (Button) view.findViewById(R.id.date_picker_button);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment(callbackInstance);
                datePickerFragment.show(getFragmentManager(), "datePickerFragment");
            }
        });

        chosenDate = (TextView) view.findViewById(R.id.chosen_date);

        timePickerButton = (Button) view.findViewById(R.id.time_picker_button);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(callbackInstance);
                timePickerFragment.show(getFragmentManager(), "timePickerFragment");
            }
        });

        chosenTime = (TextView) view.findViewById(R.id.chosen_time);

        taskTitle = (EditText) view.findViewById(R.id.task_title);

        taskNotes = (EditText) view.findViewById(R.id.task_notes);

        return alertDialog;

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        chosenDate.setText(cal.getTime().toString());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;

        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, hourOfDay, minute);
        chosenTime.setText(cal.getTime().toString());
    }
}
