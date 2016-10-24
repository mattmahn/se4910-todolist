package com.mahnke.todolist;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar now = Calendar.getInstance();
        final int day = now.get(Calendar.DAY_OF_MONTH),
                month = now.get(Calendar.MONTH),
                year = now.get(Calendar.YEAR);
        return new DatePickerDialog(getActivity(),
                                    (DatePickerDialog.OnDateSetListener) getActivity(),
                                    year,
                                    month,
                                    day);
    }

}
