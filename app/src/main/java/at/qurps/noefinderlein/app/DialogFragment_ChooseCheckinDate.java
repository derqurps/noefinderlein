package at.qurps.noefinderlein.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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


    private Callbacks mCallbacks = sDummyCallbacks;

    private  Context mContext;
    private DestinationsDB db;
    public static final String ARG_ITEMID = "item_id" ;
    public static final String ARG_YEAR = "item_year" ;
    public static final String ARG_LAT = "item_lat" ;
    public static final String ARG_LON = "item_lon" ;

    private int year = -1;
    private int id = -1;
    private double lat = -1;
    private double lon = -1;

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected_DialogFragment_ChooseCheckinDate(int id);
    }

    /**
     * A dummy implementation of the {@link DialogFragment_ChooseCheckinDate.Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected_DialogFragment_ChooseCheckinDate(int id) {
        }
    };

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

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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

        builder.setTitle(R.string.visitedChooseWhen);
        // don't allow heute when year is not current year
        if(heute>minDate && heute<maxDate && year==(int)calendar3.get(Calendar.YEAR)) {
            builder.setPositiveButton(R.string.visitedtoday, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    setVisited(new Date());
                }
            });
        }
        builder.setNegativeButton(R.string.visitedchoose, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Calendar newCalendar = Calendar.getInstance();
                DatePickerDialog newFragment = new DatePickerDialog(mContext,new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        setVisited(newDate.getTime());

                    }

                },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));


                newFragment.getDatePicker().setMinDate(minDate);
                newFragment.getDatePicker().setMaxDate(maxDate);

                newFragment.show();
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        return builder.create();
    }

    private void setVisited(Date checkDate){
        if(this.id != -1 && this.year != -1){
            String format = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String date = sdf.format(checkDate);
            DB_Visited_Locations visited = new DB_Visited_Locations(this.id, this.year, date, (float)this.lat, (float)this.lon);
            if(db.insertVisitedData(visited)){
                Util.setToast(mContext, mContext.getResources().getString(R.string.visitedChecked), 0);
                mCallbacks.onItemSelected_DialogFragment_ChooseCheckinDate(this.id);
            }else{
                Util.setToast(mContext, mContext.getResources().getString(R.string.visitedNotChecked), 0);
            }
        }else{
            Util.setToast(getActivity(), "Error getting the id & year",0);
        }
    }
}
