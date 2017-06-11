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
import me.jakemoritz.tasking.api.tasks.AddTaskTask;
import me.jakemoritz.tasking.fragment.TaskListFragment;
import me.jakemoritz.tasking.helper.DateFormatter;


public class AddTaskDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = AddTaskDialogFragment.class.getSimpleName();

    private final AddTaskDialogFragment callbackInstance = this;
    private Fragment parentFragment;

    // Date values
    private int year;
    private int month;
    private int dayOfMonth;
    private long timeInMs;

    // Views
    private EditText taskTitle;
    private EditText taskNotes;
    private TextView chosenDate;

    public static AddTaskDialogFragment newInstance(Fragment parentFragment) {
        AddTaskDialogFragment addTaskDialogFragment = new AddTaskDialogFragment();
        addTaskDialogFragment.parentFragment = parentFragment;
        addTaskDialogFragment.setRetainInstance(true);
        return addTaskDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(parentFragment.getActivity()).inflate(R.layout.dialog_add_task, (ViewGroup) null);

        // Initialize views
        chosenDate = (TextView) view.findViewById(R.id.chosen_date);
        taskTitle = (EditText) view.findViewById(R.id.task_title);
        taskNotes = (EditText) view.findViewById(R.id.task_notes);
        Button datePickerButton = (Button) view.findViewById(R.id.date_picker_button);

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

        // Set positive button OnClickListener here to ensure dialog doesn't close on click
        // Allow EditText error message to show if required
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!taskTitle.getText().toString().trim().isEmpty()) {
                            Task task = new Task();
                            task.setTitle(taskTitle.getText().toString());
                            task.setNotes(taskNotes.getText().toString());

                            // Save time in ms
                            Calendar cal = Calendar.getInstance();
                            cal.set(year, month, dayOfMonth);
                            timeInMs = cal.getTimeInMillis();
                            DateTime dateTime = new DateTime(timeInMs);

                            task.setDue(dateTime);

                            AddTaskTask addTaskTask = new AddTaskTask(getActivity(), (TaskListFragment) parentFragment, task);
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
                DatePickerDialogFragment datePickerDialogFragment = DatePickerDialogFragment.newInstance(callbackInstance);
                datePickerDialogFragment.show(getFragmentManager(), null);
            }
        });

        return alertDialog;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    public void displayCurrentDate() {
        // Get new Calendar instance
        Calendar cal = Calendar.getInstance();

        // Save current date values
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH);
        this.dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        chosenDate.setText(DateFormatter.getInstance().formatDate(cal));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Save set date values
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;

        chosenDate.setText(DateFormatter.getInstance().formatDate(dayOfMonth, month, year));
    }
}
