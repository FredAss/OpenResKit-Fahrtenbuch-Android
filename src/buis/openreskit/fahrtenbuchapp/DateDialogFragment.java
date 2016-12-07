package buis.openreskit.fahrtenbuchapp;

import android.app.DatePickerDialog;

import android.app.DatePickerDialog.OnDateSetListener;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;

import android.os.Bundle;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;


public class DateDialogFragment extends DialogFragment {
    private Fragment mFragment;

    public DateDialogFragment(Fragment callback) {
        mFragment = callback;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Date date = new Date();
        SimpleDateFormat formatday = new SimpleDateFormat("dd", Locale.GERMAN);
        Integer day = Integer.parseInt(formatday.format(date));
        SimpleDateFormat formatmonth = new SimpleDateFormat("MM", Locale.GERMAN);
        Integer month = Integer.parseInt(formatmonth.format(date));
        SimpleDateFormat formatyear = new SimpleDateFormat("yyyy", Locale.GERMAN);
        Integer year = Integer.parseInt(formatyear.format(date));

        return new DatePickerDialog(getActivity(), (OnDateSetListener) mFragment, year, month, day);
    }
}
