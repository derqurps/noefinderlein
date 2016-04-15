package at.qurps.noefinderlein.app;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import at.qurps.noefinderlein.app.Util;

public class ArrayAdapter_Near extends ArrayAdapter<Location_NoeC> {

    //private final List<Location> list;
    private List<Location_NoeC> originalData = null;
    private List<Location_NoeC> filteredData = null;
    private final Context context;
    private static final String TAG = "Distancelist-ArrayAdapter";
    private Location actuallocation;

    public ArrayAdapter_Near(Context context, List<Location_NoeC> list,Location lastlocation) {
        super(context, R.layout.listitem_near, list);
        this.context = context;
        this.originalData = list;
        this.filteredData = list;

        if(lastlocation!=null)
        {
            refreshlist(lastlocation);
        }
    }

    static class ViewHolder {
        protected TextView sortnumber;
        protected TextView ort;
        protected TextView name;
        protected TextView distance_to_destination;
        protected ImageView destinationarrow;
        protected ImageView burgstiftusw;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = LayoutInflater.from(context);
            view = inflator.inflate(R.layout.listitem_near, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.sortnumber = (TextView) view.findViewById(R.id.menuitem_sortnumber);
            viewHolder.ort = (TextView) view.findViewById(R.id.menuitem_untertitel);
            viewHolder.name = (TextView) view.findViewById(R.id.menuitem_name);
            viewHolder.distance_to_destination = (TextView) view.findViewById(R.id.distancetodestination);
            viewHolder.destinationarrow = (ImageView) view.findViewById(R.id.compastodest);
            viewHolder.burgstiftusw = (ImageView) view.findViewById(R.id.burgschloessusw);

            view.setTag(viewHolder);
            //viewHolder.checkbox.setTag(list.get(position));
        } else {
            view = convertView;
            //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        //Log.d(TAG,String.valueOf(list.get(position).getSort()));
        int nummer = filteredData.get(position).getNummer();
        if(nummer!= 0) {
            holder.sortnumber.setVisibility(View.VISIBLE);
            holder.sortnumber.setText(String.valueOf(nummer));
        }else{
            holder.sortnumber.setVisibility(View.GONE);
        }
        holder.name.setText(filteredData.get(position).getName());
        holder.ort.setText(filteredData.get(position).getAdr_ort());

        holder.distance_to_destination.setText(Util.getmkmdistance(filteredData.get(position).getDistance()));

        int category = filteredData.get(position).getKat();
        switch (category)
        {
            case 1:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_stifte);
                break;
            }
            case 2:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_burgen_schloesser);
                break;
            }
            case 3:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_museen_ausstellungen);
                break;
            }
            case 4:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_erlebnis_natur);
                break;
            }
            case 5:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_sport_und_freizeit);
                break;
            }
            case 6:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_bergbahnen);
                break;
            }
            case 7:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_schifffahrt);
                break;
            }
            case 8:
            {
                holder.burgstiftusw.setImageResource(R.mipmap.ic_lokalbahn);
                break;
            }
        }
        return view;
    }
    @Override
    public void notifyDataSetChanged() {
        //do your sorting here
        sort();
        super.notifyDataSetChanged();
    }

    public void sort() {
        Collections.sort(filteredData, new Comparator<Location_NoeC>() {
            @Override
            public int compare(Location_NoeC item1, Location_NoeC item2) {
                return Double.valueOf(item2.getDistance()).compareTo(item1.getDistance()) * (-1);
            }
        });
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
    public int getJahrtoPosition(int position) {
        return filteredData.get(position).getJahr();
    }
    public void refreshlist(Location actuallocation) {
        List<Location_NoeC> list=originalData;
        if (actuallocation!=null)
        {
            for (int i=0;i<list.size();i++)
            {
                Location zwischenloc=new Location ("");
                zwischenloc.setLatitude(list.get(i).getLatitude());
                zwischenloc.setLongitude(list.get(i).getLongitude());
                //list.get(i).setDistance(actuallocation.distanceTo(zwischenloc)); //Error in manchen Android Versionen
                list.get(i).setDistance(Util.distance_between(zwischenloc,actuallocation));
            }
        }
        else
        {
            Toast.makeText(context, R.string.hint_no_network_location, Toast.LENGTH_LONG).show();
        }
        this.filteredData = list ;
        this.originalData = list ;
        notifyDataSetChanged();

    }


}

