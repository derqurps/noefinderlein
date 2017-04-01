package at.qurps.noefinderlein.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import at.qurps.noefinderlein.app.Util;

public class ArrayAdapter_Near extends ArrayAdapter<DB_Location_NoeC> {

    //private final List<Location> list;
    private List<DB_Location_NoeC> originalData = null;
    private List<DB_Location_NoeC> filteredData = null;
    private final Context context;
    private static final String TAG = "Distancelist-ArrayAdapter";
    private Location actuallocation;
    private static SharedPreferences prefs;
    private DestinationsDB db;
    private String fDate;
    private String filterstring;
    private boolean[] filtertyp;
    private boolean filterOpen = Boolean.FALSE;
    private Location lastLocation = null;
    private int anz;

    public ArrayAdapter_Near(Context context, List<DB_Location_NoeC> list,Location lastlocation) {
        super(context, R.layout.listitem_near, list);
        this.context = context;
        this.originalData = list;
        this.filteredData = list;
        this.anz = DialogFragment_FilterList.anz;

        this.prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.db= new DestinationsDB(this.context);
        Date cDate = new Date();
        this.fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
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
        protected LinearLayout greyout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrix matrixColor = new ColorMatrix();
        matrixColor.setSaturation(1);

        ColorMatrixColorFilter colorFilterGrey = new ColorMatrixColorFilter(matrix);
        ColorMatrixColorFilter colorFilterColor = new ColorMatrixColorFilter(matrixColor);

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
            viewHolder.greyout = (LinearLayout) view.findViewById(R.id.greyout);

            view.setTag(viewHolder);
            //viewHolder.checkbox.setTag(list.get(position));
        } else {
            view = convertView;
            //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        //Log.d(TAG,String.valueOf(list.get(position).getSort()));

        boolean useOpenData = this.prefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, false);
        if(useOpenData) {
            if(filteredData.get(position).getTodayActive()) {
                holder.greyout.setVisibility(View.GONE);
                holder.sortnumber.setBackgroundColor(ContextCompat.getColor(this.context, R.color.noecard_orange_dark));
                holder.name.setTextColor(ContextCompat.getColor(this.context, R.color.noecard_orange_dark));
                holder.ort.setTextColor(ContextCompat.getColor(this.context, R.color.noecard_menu_subtext));
                holder.burgstiftusw.setColorFilter(colorFilterColor);


            } else {
                /*holder.greyout.setVisibility(View.VISIBLE);*/
                holder.sortnumber.setBackgroundColor(ContextCompat.getColor(this.context, R.color.noecard_text_grey));
                holder.name.setTextColor(ContextCompat.getColor(this.context, R.color.noecard_text_grey));
                holder.ort.setTextColor(ContextCompat.getColor(this.context, R.color.noecard_text_grey));
                holder.burgstiftusw.setColorFilter(colorFilterGrey);
            }
        }

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
        Collections.sort(filteredData, new Comparator<DB_Location_NoeC>() {
            @Override
            public int compare(DB_Location_NoeC item1, DB_Location_NoeC item2) {
                return Double.valueOf(item2.getDistance()).compareTo(item1.getDistance()) * (-1);
            }
        });
    }
    public String getactiveDay() {
        return this.fDate;
    }
    @Override
    public int getCount() {
        return filteredData!=null ? filteredData.size() : 0;
    }

    public DB_Location_NoeC getLocationtoPosition(int position) {
        return filteredData.get(position);
    }
    public String getItemsString()
    {
        String returnstring="";
        for (int i = 0; i < filteredData.size(); i++) {
            returnstring=returnstring+String.valueOf(filteredData.get(i).getId())+";";
        }
        return returnstring;
    }
    public void filterwithtyp(boolean[] filtertyploc)
    {
        filterwithtyp(filtertyploc, false);
    }
    public void filterwithtyp(boolean[] filtertyploc, boolean openfilter){
        filtertyp = filtertyploc;
        filterOpen = openfilter;
        executefilter();
        refreshlist(lastLocation);
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
        refreshlist(lastLocation);
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
        List<DB_Location_NoeC> list=filteredData;
        if (actuallocation!=null)
        {
            lastLocation=actuallocation;
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
        notifyDataSetChanged();

    }

    private void executefilter()
    {
        boolean[] filterbool= filtertyp;

        boolean filterVisited = this.prefs.getBoolean(Activity_Settings.KEY_PREF_FILTER_VISITED, false);
        boolean useOpenData = this.prefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, true);
        List<DB_Location_NoeC> firstlist;
        DB_Location_NoeC filterableLocation ;

        if(filterVisited) {
            final List<DB_Location_NoeC> zwlist = originalData;
            int countZw = zwlist.size();
            final ArrayList<DB_Location_NoeC> zwzwlist = new ArrayList<DB_Location_NoeC>(countZw);
            for (int i = 0; i < countZw; i++) {
                filterableLocation = zwlist.get(i);
                if (!this.db.isVisited(filterableLocation.getId())) {
                    zwzwlist.add(filterableLocation);
                }
            }
            firstlist = zwzwlist;
        } else {
            firstlist = originalData;
        }
        if(filterOpen && useOpenData) {
            final List<DB_Location_NoeC> zwlistTwo = firstlist;
            int countZw = zwlistTwo.size();
            final ArrayList<DB_Location_NoeC> zwzwlistTwo = new ArrayList<DB_Location_NoeC>(countZw);
            for (int i = 0; i < countZw; i++) {
                filterableLocation = zwlistTwo.get(i);
                if (filterableLocation.getTodayActive()) {
                    zwzwlistTwo.add(filterableLocation);
                }
            }
            firstlist = zwzwlistTwo;
        }

        int count = firstlist.size();
        final ArrayList<DB_Location_NoeC> firstnlist = new ArrayList<DB_Location_NoeC>(count);
        boolean zusfilt = true;
        boolean zusfiltTop = true;
        for (boolean t:filterbool) {
            if(t){
                zusfilt = false;
                break;
            }
        }
        if(filterbool.length>anz-1) {
            for (int i = 0; i < anz; i++) {
                if(filterbool[i]){
                    zusfiltTop = false;
                    break;
                }
            }
        }

        for (int i = 0; i < count; i++) {

            filterableLocation = firstlist.get(i);
            int[] bla = {0,0,0,0,0,0,0,0,0,0,0,0,0};
            if(zusfilt){
                firstnlist.add(filterableLocation);
                continue;
            }


            if(filterbool.length>8 && filterbool[8]) {
                if (filterableLocation.getHund()) {
                    bla[8]=1;
                }else {
                    bla[8]=2;
                }
            }
            if(filterbool.length>9 && filterbool[9]) {
                if (filterableLocation.getRollstuhl()) {
                    bla[9]=1;
                }else {
                    bla[9]=2;
                }
            }
            if(filterbool.length>10 && filterbool[10]) {
                if (filterableLocation.getKinderwagen()) {
                    bla[10]=1;
                }else {
                    bla[10]=2;
                }
            }
            if(filterbool.length>11 && filterbool[11]) {
                if (filterableLocation.getGruppe()) {
                    bla[11]=1;
                }else {
                    bla[11]=2;
                }
            }
            if(filterbool.length>12 && filterbool[12]) {
                if (filterableLocation.getTop_ausflugsziel()) {
                    bla[12]=1;
                }else {
                    bla[12]=2;
                }
            }
            boolean minZusFilter = true;
            for (int j = 8; j < bla.length; j++) {
                if(bla[j]==2){
                    minZusFilter = false;
                    break;
                }
            }
            if(!minZusFilter){
                continue;
            }
            if(zusfiltTop){
                firstnlist.add(filterableLocation);
            }else {
                int category = filterableLocation.getKat();
                for (int j = 1; j < filterbool.length; j++) {
                    if (filterbool[j - 1] && category == j) {
                        firstnlist.add(filterableLocation);
                        break;
                    }
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

