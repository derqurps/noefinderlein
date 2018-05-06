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

public class ArrayAdapter_Visited extends ArrayAdapter<DB_Visited_ArrayAdapter> {

    //private final List<Location> list;
    private List<DB_Visited_ArrayAdapter> originalData = null;
    private List<DB_Visited_ArrayAdapter> filteredData = null;
    private final Context context;
    private String filterstring;
    private boolean[] filtertyp;
    private static final String TAG = "VisitedArrayAdapter";

    public ArrayAdapter_Visited(Context context, List<DB_Visited_ArrayAdapter> list) {
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
        int nummer = filteredData.get(position).getLoc().getNummer();
        if(nummer!= 0) {
            holder.sortnumber.setVisibility(View.VISIBLE);
            holder.sortnumber.setText(String.valueOf(nummer));
        }else{
            holder.sortnumber.setVisibility(View.GONE);
        }
        //holder.sortnumber.setText(String.valueOf(filteredData.get(position).getNummer()));
        holder.name.setText(filteredData.get(position).getLoc().getName());
        holder.ort.setText(filteredData.get(position).getLoc().getAdr_ort());

        holder.ersparnis.setText(String.format(context.getString(R.string.currencyformatString), String.valueOf(filteredData.get(position).getVis().getSaved()).replace(',',Character.MIN_VALUE).replace('.',',')));

        holder.date.setText(filteredData.get(position).getVis().getDate());
        return view;
    }
    @Override
    public int getCount() {
        return filteredData!=null ? filteredData.size() : 0;
    }
    public DB_Visited_ArrayAdapter getLocationtoPosition(int position) {
        return filteredData.get(position);
    }
    public int getNumbertoPosition(int position) {
        return filteredData.get(position).getLoc().getNummer();
    }
    public int getIdtoPosition(int position) {
        return filteredData.get(position).getLoc().getId();
    }
    public int getVIdtoPosition(int position) {
        return filteredData.get(position).getVis().getId();
    }
    public int getJahrtoPosition(int position) {
        return filteredData.get(position).getLoc().getJahr();
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
            returnstring=returnstring+String.valueOf(filteredData.get(i).getLoc().getId())+";";
        }
        return returnstring;
    }
    public int getItemsYear() {
        int returnyear=0;
        returnyear=filteredData.get(0).getLoc().getJahr();
        return returnyear;
    }

    private void executefilter()
    {
        boolean[] filterbool= filtertyp;

        final List<DB_Visited_ArrayAdapter> firstlist = originalData;
        int count = firstlist.size();
        final ArrayList<DB_Visited_ArrayAdapter> firstnlist = new ArrayList<DB_Visited_ArrayAdapter>(count);

        DB_Visited_ArrayAdapter filterableLocation ;
        for (int i = 0; i < count; i++) {
            filterableLocation = firstlist.get(i);

            if ( (filterbool[0] && filterableLocation.getLoc().getTop_ausflugsziel()) ) {
                firstnlist.add(filterableLocation);
                continue;
            }

            for(int j=1;j<filterbool.length;j++)
            {
                int category = filterableLocation.getLoc().getKat();
                if(filterbool[j] && category==j){
                    firstnlist.add(filterableLocation);
                    break;
                }
            }
        }


        int nummer;
        String filterString = filterstring;
        final List<DB_Visited_ArrayAdapter> list = firstnlist;
        if(filterString != null && filterString.length() > 0)
        {
            count = list.size();
            final ArrayList<DB_Visited_ArrayAdapter> nlist = new ArrayList<DB_Visited_ArrayAdapter>(count);

            try
            {
                nummer = Integer.valueOf(filterstring);
                for (int i = 0; i < count; i++) {
                    filterableLocation = list.get(i);

                    if (filterableLocation.getLoc().getNummer()==nummer) {
                        nlist.add(filterableLocation);
                    }
                }
            }
            catch  (Exception e)
            {
                for (int i = 0; i < count; i++) {
                    filterableLocation = list.get(i);

                    if (filterableLocation.getLoc().getName().toLowerCase().contains(filterString)) {
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
