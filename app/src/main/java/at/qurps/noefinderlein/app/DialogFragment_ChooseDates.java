package at.qurps.noefinderlein.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import java.util.Calendar;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

/**
 * Created by roman on 01.04.17.
 */

public class DialogFragment_ChooseDates extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(int year, int month, int day);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;


    public static final String TAG = "DialogFragment_ChooseDates";

    public static final String YEAR = TAG + "-YEAR";
    public static final String MONTH = TAG + "-MONTH";
    public static final String DAY = TAG + "-DAY";

    private SharedPreferences sharedPref;

    public DialogFragment_ChooseDates() {

    }
    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }*/
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar cNow = Calendar.getInstance();
        int year = Util.getDatePreferencesYear(getActivity());
        int month = Util.getDatePreferencesMonth(getActivity());
        int day = Util.getDatePreferencesDay(getActivity());
        if (year == 0 || day == 0) {
            year = cNow.get(Calendar.YEAR);
            month = cNow.get(Calendar.MONTH);
            day = cNow.get(Calendar.DAY_OF_MONTH);
        }

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.visitedtoday), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Your code
                Calendar cNow = Calendar.getInstance();
                int year = cNow.get(Calendar.YEAR);
                int month = cNow.get(Calendar.MONTH);
                int day = cNow.get(Calendar.DAY_OF_MONTH);
                mListener.onDialogPositiveClick(year, month, day);
            }
        });

        return dialog;

    }
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        Calendar cNow = Calendar.getInstance();
        Calendar cinput = Calendar.getInstance();
        cinput.set(year, month, day);
        if(cinput.before(cNow))
        {
            year = cNow.get(Calendar.YEAR);
            month = cNow.get(Calendar.MONTH);
            day = cNow.get(Calendar.DAY_OF_MONTH);
        }
        mListener.onDialogPositiveClick(year, month, day);
    }
    public void setListener(NoticeDialogListener listener) {
        mListener = listener;
    }

}
