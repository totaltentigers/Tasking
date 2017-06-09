package me.jakemoritz.tasking.dialog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.Calendar;

import me.jakemoritz.tasking.R;
import me.jakemoritz.tasking.api.tasks.EditTaskTask;
import me.jakemoritz.tasking.fragment.TaskListFragment;
import me.jakemoritz.tasking.helper.DateFormatter;


public class EditTaskDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    private static final String TAG = EditTaskDialogFragment.class.getSimpleName();

    private final EditTaskDialogFragment callbackInstance = this;

    private Fragment parentFragment;
    private Task task;

    // Date values
    private int year;
    private int month;
    private int dayOfMonth;
    private long timeInMs;

    // Views
    private EditText taskTitle;
    private EditText taskNotes;
    private TextView chosenDate;

    public static EditTaskDialogFragment newInstance(Fragment parentFragment, Task task) {
        EditTaskDialogFragment editTaskDialogFragment = new EditTaskDialogFragment();
        editTaskDialogFragment.parentFragment = parentFragment;
        editTaskDialogFragment.task = task;
        return editTaskDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(parentFragment.getActivity()).inflate(R.layout.dialog_add_task, (ViewGroup) null);

        // Initialize views
        chosenDate = (TextView) view.findViewById(R.id.chosen_date);
        taskTitle = (EditText) view.findViewById(R.id.task_title);
        taskNotes = (EditText) view.findViewById(R.id.task_notes);
        Button datePickerButton = (Button) view.findViewById(R.id.date_picker_button);

        // Update views with task values
        taskTitle.setText(task.getTitle());
        taskNotes.setText(task.getNotes());

        displayTaskDueDate();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(getString(R.string.edit_task_dialog_edit))
                .setPositiveButton(R.string.edit_task_dialog_save, null)
                .setNegativeButton(R.string.add_task_dialog_cancel, new DialogInterface.OnClickListener() {
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
                        if (!taskTitle.getText().toString().trim().isEmpty()) {
                            task.setTitle(taskTitle.getText().toString());
                            task.setNotes(taskNotes.getText().toString());

                            if (!chosenDate.getText().toString().isEmpty()){
                                // Save time in ms
                                Calendar cal = Calendar.getInstance();
                                cal.set(year, month, dayOfMonth);
                                timeInMs = cal.getTimeInMillis();

                                DateTime dateTime = new DateTime(timeInMs);
                                task.setDue(dateTime);
                            }

                            EditTaskTask editTaskTask = new EditTaskTask(getActivity(), (TaskListFragment) parentFragment, task);
                            editTaskTask.execute();

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
                DatePickerDialogFragment datePickerDialogFragment = DatePickerDialogFragment.newInstance(callbackInstance);
                datePickerDialogFragment.show(getFragmentManager(), null);
            }
        });

        return alertDialog;
    }

    public void displayTaskDueDate(){
        if (task.getDue() != null){
            chosenDate.setText(DateFormatter.getInstance().formatDate(task.getDue()));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // Save set date values
        this.year = year;
        this.month = monthOfYear;
        this.dayOfMonth = dayOfMonth;

        chosenDate.setText(DateFormatter.getInstance().formatDate(dayOfMonth, monthOfYear, year));
    }
}
