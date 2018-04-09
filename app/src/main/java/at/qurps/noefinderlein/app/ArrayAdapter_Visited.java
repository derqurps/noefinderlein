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

public class ArrayAdapter_Visited extends ArrayAdapter<DB_Location_NoeC> {

    //private final List<Location> list;
    private List<DB_Location_NoeC> originalData = null;
    private List<DB_Location_NoeC> filteredData = null;
    private final Context context;
    private String filterstring;
    private boolean[] filtertyp;
    private static final String TAG = "VisitedArrayAdapter";

    public ArrayAdapter_Visited(Context context, List<DB_Location_NoeC> list) {
        super(context, R.layout.listitem_visited, list);
        this.context = context;
        //this.list = list;
        this.filteredData = list ;
        this.originalData = list ;
    }

    static class ViewHolder {
        protected TextView sortnumber;
        protected TextView ort;
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
            viewHolder.ort = (TextView) view.findViewById(R.id.menuitem_untertitel);
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
        //HyperLog.d(TAG,String.valueOf(list.get(position).getSort()));
        int nummer = filteredData.get(position).getNummer();
        if(nummer!= 0) {
            holder.sortnumber.setVisibility(View.VISIBLE);
            holder.sortnumber.setText(String.valueOf(nummer));
        }else{
            holder.sortnumber.setVisibility(View.GONE);
        }
        //holder.sortnumber.setText(String.valueOf(filteredData.get(position).getNummer()));
        holder.name.setText(filteredData.get(position).getName());
        holder.ort.setText(filteredData.get(position).getAdr_ort());

        holder.ersparnis.setText(filteredData.get(position).getErsparnis());

        holder.date.setText(filteredData.get(position).getVisited_date());
        return view;
    }
    @Override
    public int getCount() {
        return filteredData!=null ? filteredData.size() : 0;
    }
    public DB_Location_NoeC getLocationtoPosition(int position) {
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

    private void executefilter()
    {
        boolean[] filterbool= filtertyp;

        final List<DB_Location_NoeC> firstlist = originalData;
        int count = firstlist.size();
        final ArrayList<DB_Location_NoeC> firstnlist = new ArrayList<DB_Location_NoeC>(count);

        DB_Location_NoeC filterableLocation ;
        for (int i = 0; i < count; i++) {
            filterableLocation = firstlist.get(i);

            if ( (filterbool[0] && filterableLocation.getTop_ausflugsziel()) ) {
                firstnlist.add(filterableLocation);
                continue;
            }

            for(int j=1;j<filterbool.length;j++)
            {
                int category = filterableLocation.getKat();
                if(filterbool[j] && category==j){
                    firstnlist.add(filterableLocation);
                    break;
                }
            }
        }


        int nummer;
        String filterString = filterstring;
        final List<DB_Location_NoeC> list = firstnlist;
        if(filterString != null && filterString.length() > 0)
        {
            count = list.size();
            final ArrayList<DB_Location_NoeC> nlist = new ArrayList<DB_Location_NoeC>(count);

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
