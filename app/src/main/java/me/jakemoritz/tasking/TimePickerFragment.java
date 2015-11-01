package me.jakemoritz.tasking;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    private AddTaskDialogFragment addTaskDialogFragment;

    public TimePickerFragment(AddTaskDialogFragment addTaskDialogFragment) {
        super();
        this.addTaskDialogFragment = addTaskDialogFragment;
    }

    private static final String TAG = "TimePickerFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use current date as default date
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), addTaskDialogFragment, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
    }
}
