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
 * Created by roman on 11.04.16.
 */
public class DialogFragment_ChooseCheckinDate extends DialogFragment {


    private  Context mContext;
    private DestinationsDB db;
    public static final String ARG_ITEMID = "item_id" ;
    public static final String ARG_YEAR = "item_year" ;
    private int year = -1;
    private int id = -1;

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

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, 3, 1, 0, 0, 0);
        final long minDate = calendar.getTimeInMillis();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(year+1, 2, 31, 23, 59, 0);
        final long maxDate = calendar2.getTimeInMillis();
        Calendar calendar3 = Calendar.getInstance();
        long heute = calendar3.getTimeInMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.visitedChooseWhen);
        // don't allow heute when year is not current year
        if(heute>minDate && heute<maxDate && year==calendar3.get(Calendar.YEAR)) {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                        newFragment.getDatePicker().setMinDate(minDate);
                        newFragment.getDatePicker().setMaxDate(maxDate);
                    }
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
            DB_Visited_Locations visited = new DB_Visited_Locations(this.id,this.year,date);
            if(db.insertVisitedData(visited)){
                Util.setToast(mContext, mContext.getResources().getString(R.string.visitedChecked),0);
            }else{
                Util.setToast(mContext, mContext.getResources().getString(R.string.visitedNotChecked),0);
            }
        }else{
            Util.setToast(getActivity(), "Error getting the id & year",0);
        }
    }
}
