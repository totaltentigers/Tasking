package me.jakemoritz.tasking_new.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment {

    private static final String TAG = DatePickerDialogFragment.class.getSimpleName();

    private DatePickerDialog.OnDateSetListener onDateSetListener;

    public static DatePickerDialogFragment newInstance(DatePickerDialog.OnDateSetListener onDateSetListener) {
        DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
        datePickerDialogFragment.onDateSetListener = onDateSetListener;
        datePickerDialogFragment.setRetainInstance(true);
        return datePickerDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use current date as default date
        Calendar currentCal = Calendar.getInstance();
        int year = currentCal.get(Calendar.YEAR);
        int month = currentCal.get(Calendar.MONTH);
        int day = currentCal.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
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
}
