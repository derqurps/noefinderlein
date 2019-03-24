package at.qurps.noefinderlein.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;


import com.hypertrack.hyperlog.HyperLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A list fragment representing a list of Locations. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 *
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class DialogFragment_ChooseCheckinDate extends DialogFragment {

    private Callbacks mCallbacks;

    private Context mContext;
    private DestinationsDB db;
    public static final String ARG_ITEMID = "item_id" ;
    public static final String ARG_YEAR = "item_year" ;
    public static final String ARG_LAT = "item_lat" ;
    public static final String ARG_LON = "item_lon" ;
    public static final String ARG_AMOUNT = "item_ersparnis" ;

    private static final String TAG = "DF_ChooseCheckinDate";

    private int year = -1;
    private int id = -1;
    private double lat = -1;
    private double lon = -1;
    private float amount = 0;
    long chosenDate = 0;

    public interface Callbacks {
        void onItemSelected_DialogFragment_ChooseCheckinDate(int id);
    }

    public DialogFragment_ChooseCheckinDate() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext=getActivity();
        this.db= new DestinationsDB(mContext);
        if(getArguments().containsKey(ARG_YEAR)){
            year = getArguments().getInt(ARG_YEAR);
        }
        if(getArguments().containsKey(ARG_ITEMID)){
            id = getArguments().getInt(ARG_ITEMID);
        }
        if(getArguments().containsKey(ARG_LAT)){
            lat = getArguments().getDouble(ARG_LAT);
        }
        if(getArguments().containsKey(ARG_LON)){
            lon = getArguments().getDouble(ARG_LON);
        }
        if(getArguments().containsKey(ARG_AMOUNT)){
            amount = getArguments().getFloat(ARG_AMOUNT);
        }

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        // Activities containing this fragment must implement its callbacks.
        if (!(context instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View inflView = inflater.inflate(R.layout.dialog_choose_checkin_date, null);

        Calendar nowCalendar = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, 3, 1, 0, 0, 0);
        final long minDate = calendar.getTimeInMillis();
        Calendar calendar2 = Calendar.getInstance();
        //calendar2.set(year+1, 2, 31, 23, 59, 0);
        calendar2.set(nowCalendar.get(Calendar.YEAR), nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
        final long maxDate = calendar2.getTimeInMillis();
        Calendar calendar3 = Calendar.getInstance();
        long heute = calendar3.getTimeInMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflView);
        builder.setTitle(R.string.visitedChooseWhen);

        CalendarView cv = (CalendarView) inflView.findViewById(R.id.checkinCalendarView);
        final EditText pa = (EditText) inflView.findViewById(R.id.payedAmountEdit);
        cv.setMaxDate(maxDate);
        cv.setMinDate(minDate);
        pa.setText( String.format(Locale.GERMANY, "%.2f", amount));
        long chosenDate;
        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                setChosenDate(cal.getTimeInMillis());
                HyperLog.d(TAG, String.valueOf(year) + String.valueOf(month)+ String.valueOf(dayOfMonth) + String.valueOf(cal.getTimeInMillis()));
            }
        });

        // don't allow heute when year is not current year
        /*if(heute>minDate && heute<maxDate && year==(int)calendar3.get(Calendar.YEAR)) {
            builder.setPositiveButton(R.string.visitedtoday, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    setVisited(new Date());
                }
            });
        }*/
        builder.setPositiveButton(R.string.visitedchoose, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                Editable TextInput = pa.getText();
                String selectedDate = getChosenDate();
                float parsed = Util.moneyToFloat(TextInput.toString());
                Util.setToast(getActivity(), "Saving € " + ' ' + String.valueOf(parsed) + " on " + selectedDate,0);
                setVisited(selectedDate, parsed);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        /*builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

        });*/

        return builder.create();
    }
    private void setChosenDate(long date) {
        chosenDate = date;
    }
    private String getChosenDate() {
        Date newchosenDate;
        if (chosenDate == 0) {
            newchosenDate = new Date();
        } else {
            newchosenDate = new Date(chosenDate);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        return sdf.format(newchosenDate);
    }
    private void setVisited(String checkDate, float saved){
        if(this.id != -1 && this.year != -1){
            DB_Visited_Locations visited = new DB_Visited_Locations(this.id, this.year, checkDate, (float)this.lat, (float)this.lon, saved);
            if(db.insertVisitedData(visited)){
                Util.setToast(mContext, mContext.getResources().getString(R.string.visitedChecked), 0);
                Log.d(TAG, "setVisited() calling callback: " );
                mCallbacks.onItemSelected_DialogFragment_ChooseCheckinDate(this.id);
            }else{
                Util.setToast(mContext, mContext.getResources().getString(R.string.visitedNotChecked), 0);
            }
        }else{
            Util.setToast(getActivity(), "Error getting the id & year",0);
        }
    }
}
