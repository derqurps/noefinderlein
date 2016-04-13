package at.qurps.noefinderlein.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.Toast;

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
        Dialog builder = new AlertDialog.Builder(getActivity())

            .setTitle(R.string.visitedChooseWhen)
            .setPositiveButton(R.string.visitedtoday, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    setVisited(new Date());
                }
            })
            .setNegativeButton(R.string.visitedchoose, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Calendar newCalendar = Calendar.getInstance();
                    DatePickerDialog newFragment = new DatePickerDialog(mContext,new DatePickerDialog.OnDateSetListener() {

                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar newDate = Calendar.getInstance();
                            newDate.set(year, monthOfYear, dayOfMonth);
                            setVisited(newDate.getTime());

                        }

                    },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                    newFragment.show();
                }
            })
            .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            })
            .create();
        return builder;
    }
    private void setVisited(Date checkDate){
        if(this.id != -1 && this.year != -1){
            String format = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String date = sdf.format(checkDate);
            Visited_Locations visited = new Visited_Locations(this.id,this.year,date);
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
