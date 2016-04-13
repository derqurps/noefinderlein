package at.qurps.noefinderlein.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayAdapter_Visited extends ArrayAdapter<Location_NoeC> {

    //private final List<Location> list;
    private List<Location_NoeC> originalData = null;
    private List<Location_NoeC> filteredData = null;
    private final Context context;
    private String filterstring;
    private boolean[] filtertyp;
    private static final String TAG = "VisitedArrayAdapter";

    public ArrayAdapter_Visited(Context context, List<Location_NoeC> list) {
        super(context, R.layout.listitem_visited, list);
        this.context = context;
        //this.list = list;
        this.filteredData = list ;
        this.originalData = list ;
    }

    static class ViewHolder {
        protected TextView sortnumber;
        protected TextView name;
        protected TextView ersparnis;
        protected TextView date;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = LayoutInflater.from(context);
            view = inflator.inflate(R.layout.listitem_visited, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.sortnumber = (TextView) view.findViewById(R.id.menuitem_sortnumber);
            viewHolder.name = (TextView) view.findViewById(R.id.menuitem_name);
            viewHolder.ersparnis = (TextView) view.findViewById(R.id.menuitem_ersparnis);
            viewHolder.date = (TextView) view.findViewById(R.id.menuitem_visiteddate);

            view.setTag(viewHolder);
            //viewHolder.checkbox.setTag(list.get(position));
        } else {
            view = convertView;
            //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        //Log.d(TAG,String.valueOf(list.get(position).getSort()));
        holder.sortnumber.setText(String.valueOf(filteredData.get(position).getNummer()));
        holder.name.setText(filteredData.get(position).getName());

        holder.ersparnis.setText(filteredData.get(position).getErsparnis());

        holder.date.setText(filteredData.get(position).getVisited_date());
        return view;
    }
    @Override
    public int getCount() {
        return filteredData!=null ? filteredData.size() : 0;
    }
    public Location_NoeC getLocationtoPosition(int position) {
        return filteredData.get(position);
    }
    public int getNumbertoPosition(int position) {
        return filteredData.get(position).getNummer();
    }
    public int getIdtoPosition(int position) {
        return filteredData.get(position).getId();
    }
    public int getVIdtoPosition(int position) {
        return filteredData.get(position).getVisited_id();
    }
    public int getJahrtoPosition(int position) {
        return filteredData.get(position).getJahr();
    }
    public void filterwithtyp(boolean[] filtertyploc)
    {
        filtertyp=filtertyploc;
        executefilter();
    }
    public void filterwithstring(String filterstr)
    {
        filterstring=filterstr;
        if(filtertyp==null)
        {
            filtertyp=new boolean[10];
            Arrays.fill(filtertyp, Boolean.TRUE);
        }
        executefilter();
    }
    public String getItemsString()
    {
        String returnstring="";
        for (int i = 0; i < filteredData.size(); i++) {
            returnstring=returnstring+String.valueOf(filteredData.get(i).getId())+";";
        }
        return returnstring;
    }
    public int getItemsYear() {
        int returnyear=0;
        returnyear=filteredData.get(0).getJahr();
        return returnyear;
    }
    public String getGesamtErsparnis() {
        float ersparnis = 0;
        Log.d(TAG, "hier -2");
        for (int i = 0; i < filteredData.size(); i++) {
            try {
                Log.d(TAG, String.valueOf(i));
                String ersparnis_str = filteredData.get(i).getErsparnis();
                String[] splitResult = ersparnis_str.split("€");
                float[] zwischarr = new float[splitResult.length-1];
                float zwischenwert = 0;
                for(int j = 1; j < splitResult.length; j++)
                {
                    Log.d(TAG,String.valueOf(splitResult[j]));
                    if(splitResult[j].indexOf("-")>-1){
                        splitResult[j]=splitResult[j].substring(0,splitResult[j].indexOf("-"));
                    }
                    splitResult[j]=splitResult[j].replace("€","").replace(" " ,"").replace("-","").replace(".","");
                    splitResult[j]=splitResult[j].replace(",",".");
                    zwischarr[j-1]=Float.valueOf(splitResult[j]).floatValue();
                    Log.d(TAG,String.valueOf(zwischarr[j-1]));
                }

                for(int j = 0; j < zwischarr.length; j++) {
                    zwischenwert = zwischenwert + zwischarr[j];
                }
                zwischenwert = zwischenwert / zwischarr.length;
                Log.d(TAG,String.valueOf(zwischenwert ));

                ersparnis = ersparnis + zwischenwert;
            }
            catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        }
        return String.valueOf(ersparnis);
    }
    private void executefilter()
    {
        boolean[] filterbool= filtertyp;

        final List<Location_NoeC> firstlist = originalData;
        int count = firstlist.size();
        final ArrayList<Location_NoeC> firstnlist = new ArrayList<Location_NoeC>(count);

        Location_NoeC filterableLocation ;
        for (int i = 0; i < count; i++) {
            filterableLocation = firstlist.get(i);

            if ( (filterbool[0] && filterableLocation.getTop_ausflugsziel()) ) {
                firstnlist.add(filterableLocation);
                continue;
            }

            for(int j=1;j<filterbool.length;j++)
            {
                String category = filterableLocation.getKat();
                int[] catIArray = Util.getIntArrayFromString(category);
                if(filterbool[j] && catIArray[0]==j){
                    firstnlist.add(filterableLocation);
                    break;
                }
            }
        }


        int nummer;
        String filterString = filterstring;
        final List<Location_NoeC> list = firstnlist;
        if(filterString != null && filterString.length() > 0)
        {
            count = list.size();
            final ArrayList<Location_NoeC> nlist = new ArrayList<Location_NoeC>(count);

            try
            {
                nummer = Integer.valueOf(filterstring);
                for (int i = 0; i < count; i++) {
                    filterableLocation = list.get(i);

                    if (filterableLocation.getNummer()==nummer) {
                        nlist.add(filterableLocation);
                    }
                }
            }
            catch  (Exception e)
            {
                for (int i = 0; i < count; i++) {
                    filterableLocation = list.get(i);

                    if (filterableLocation.getName().toLowerCase().contains(filterString)) {
                        nlist.add(filterableLocation);
                    }
                }
            }


            filteredData = nlist;
        }
        else
        {
            filteredData = list;
        }

        if (filteredData.size() > 0) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }

}
