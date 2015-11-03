package me.jakemoritz.tasking;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    private Fragment fragment;

    public DateSetResponse delegate = null;

    public DatePickerFragment(Fragment fragment) {
        super();
        this.fragment = fragment;
    }

    private static final String TAG = "DatePickerFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use current date as default date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Log.d(TAG, "month: " + monthOfYear + ", day: " + dayOfMonth + ", year: " + year);
        delegate.dateSet(year, monthOfYear, dayOfMonth);
    }
}
