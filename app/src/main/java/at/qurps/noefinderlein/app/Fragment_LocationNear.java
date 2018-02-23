package at.qurps.noefinderlein.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by roman on 02.04.16.
 */
public class Fragment_LocationNear extends ListFragment implements DialogFragment_FilterList.NoticeDialogListener{

    public static final String TAG = "Fragment_LocationNear";
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    public static final String ARG_ITEM_NUMMER = "item_nummer" ;
    public static final String ARG_ITEM_JAHR = "item_jahr" ;
    public static final String ARG_MTWOPANE ="mTwoPane" ;
    public static final String ARG_ISREGION ="isRegion" ;
    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks;
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected_Fragment_LocationNear(int i,int jahr);
    }

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private SearchView searchView;
    List<DB_Location_NoeC> listItems=new ArrayList<DB_Location_NoeC>();
    ArrayAdapter_Near adapter;
    private DestinationsDB db;
    private Context mContext;
    private boolean mTwoPane;
    public boolean mIsRegion=false;
    private boolean allowlocationupdates=false;

    public int mRegionItemJahr;
    private boolean useOpenData;
    private Location zwloc;
    private View rootView;

    private final BroadcastReceiver myBRLocaupd=new LocationUpdate();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext=getActivity();
        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);
        this.db= new DestinationsDB(mContext);
        SharedPreferences sharedPrefAct = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        this.useOpenData = sharedPrefAct.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, true);
        //getListView().
        //TODO Fastscroll enable
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_location_list, container, false);
        //container.removeAllViews();

        parseArguments(getArguments());

        ChangeView(getArguments());

        return rootView;
    }
    public void ChangeView(Bundle arguments) {
        dbContentChanged();
    }
    public void dbContentChanged() {
        listItems = db.getAllMenuDistanceLocations(mRegionItemJahr);
        adapter=new ArrayAdapter_Near(mContext,listItems,null);
        filterWithNewData(getFilterliste(), getOpenFilter());

        updateListwithnewLocation(this.zwloc);

        setListAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState
                    .getInt(STATE_ACTIVATED_POSITION));
        }

        if (((Activity_Main)this.getActivity()).mLastLocation!=null) {
            this.zwloc=((Activity_Main)this.getActivity()).mLastLocation;

            updateListwithnewLocation(this.zwloc);

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
    public void onListItemClick(ListView listView, View view, int position,
                                long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected_Fragment_LocationNear(adapter.getIdtoPosition(position), adapter.getJahrtoPosition(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        allowlocationupdates=true;
        mContext.registerReceiver(myBRLocaupd, new IntentFilter("locationupdate"));

        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        SharedPreferences sharedPrefAct = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        boolean compUseOpenData = sharedPrefAct.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, true);
        if(!fDate.equals(this.adapter.getactiveDay()) || compUseOpenData != this.useOpenData){
            // daychange - reload adapter
            dbContentChanged();
            updateListwithnewLocation(this.zwloc);
            this.useOpenData = compUseOpenData;
        }
    }
    @Override
    public void onPause() {
        allowlocationupdates=false;
        mContext.unregisterReceiver(myBRLocaupd);
        super.onPause();
    }

    public void parseArguments(Bundle arguments) {
        if (arguments!=null) {
            if (arguments.containsKey(ARG_ISREGION)) {
                mIsRegion = arguments.getBoolean(ARG_ISREGION);
            }
            if (arguments.containsKey(ARG_MTWOPANE)) {
                mTwoPane = arguments.getBoolean(ARG_MTWOPANE);

            }
            if (arguments.containsKey(ARG_ITEM_JAHR)) {
                mRegionItemJahr = arguments.getInt(ARG_ITEM_JAHR);
            }
            Log.d(TAG, "oncreate hier" + String.valueOf(mTwoPane));
        }
    }
    public class LocationUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(allowlocationupdates) {
                updateListwithnewLocation(null);
            }
        }
    }
    public void updateListwithnewLocation(Location location) {
        // TODO Auto-generated method stub
        if(location==null) {
            location = ((Activity_Main) this.getActivity()).mLastLocation;
        }

        adapter.refreshlist(location);
    }
    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_options_near, menu);

        Util.colorMenuItems(mContext, menu,R.id.actionb_filter_list, R.color.noecard_white);
        Util.colorMenuItems(mContext, menu,R.id.actionb_show_current_in_map, R.color.noecard_white);

        super.onCreateOptionsMenu(menu,inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionb_filter_list:
                DialogFragment_FilterList newFragment = new DialogFragment_FilterList();
                newFragment.setListener(Fragment_LocationNear.this);
                newFragment.show(Fragment_LocationNear.this.getActivity().getSupportFragmentManager(), "filter");
                return true;

            case R.id.actionb_show_current_in_map:

                mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

                Bundle arguments = new Bundle();
                arguments.putString(Activity_Map.ARG_ITEM_IDS, adapter.getItemsString());
                arguments.putInt(Activity_Map.ARG_ITEM_YEAR, mRegionItemJahr);
                arguments.putBoolean(Activity_Map.ARG_MTWOPANE, mTwoPane);

                Intent intent = new Intent(mContext, Activity_Map.class);
                intent.putExtras(arguments);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);

    }
    @Override
    public void onDialogPositiveClick(boolean[] filterlist, boolean openFilter) {
        // User touched the dialog's positive button
        dbContentChanged();
        filterWithNewData(filterlist, openFilter);
    }
    public void filterWithNewData(boolean[] filterlist, boolean openFilter) {
        // User touched the dialog's positive button
        //dbContentChanged();
        ActivityCompat.invalidateOptionsMenu(getActivity());
        adapter.filterwithtyp(filterlist, openFilter);
    }
    private boolean[] getFilterliste(){
        String[] dest_typ_list = getResources().getStringArray(R.array.filter_typ_list);
        boolean[] returnbool=new boolean[dest_typ_list.length];
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        for(int i=0;i<dest_typ_list.length;i++)
        {
            returnbool[i]=sharedPref.getBoolean(dest_typ_list[i], false);
        }
        return returnbool;
    }
    private boolean getOpenFilter(){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(getResources().getString(R.string.filter_open_sett), false);
    }
    private void showFilterActive(boolean filtered, String dayString){
        final TextView txt = (TextView)rootView.findViewById(R.id.mainlist_filtertext);
        txt.setText(getResources().getString(R.string.filteractive) + dayString);
        if(filtered && txt.getVisibility()==View.GONE){
            txt.setVisibility(View.VISIBLE);
            final Animation animLinearDown = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_top);
            txt.startAnimation(animLinearDown);
        }else if(!filtered && txt.getVisibility()==View.VISIBLE){
            final Animation animLinearUp = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_top);
            animLinearUp.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {}
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationEnd(Animation animation) {
                    txt.setVisibility(View.GONE);
                }
            });
            txt.startAnimation(animLinearUp);

        }
    }
    private boolean isFilterSet(){
        boolean returnval=true;
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String[] dest_typ_list = getResources().getStringArray(R.array.filter_typ_list);
        boolean noneset = true;
        for(int i=0;i<dest_typ_list.length;i++)
        {
			/*if(!sharedPref.getBoolean(dest_typ_list[i], true))
			{
				returnval=true;
			}
            else {*/
            if(sharedPref.getBoolean(dest_typ_list[i], false)){
                noneset = false;
            }
        }
        boolean filterOpen = sharedPref.getBoolean(getResources().getString(R.string.filter_open_sett), false);
        SharedPreferences sharedPrefAct = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        this.useOpenData = sharedPrefAct.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, true);
        if(filterOpen && this.useOpenData) {
            noneset = false;
        }

        String dayString = "";
        int year = Util.getDatePreferencesYear(getActivity());
        int month = Util.getDatePreferencesMonth(getActivity());
        int day = Util.getDatePreferencesDay(getActivity());

        Calendar currentDay = Calendar.getInstance();
        Calendar setDay = Calendar.getInstance();
        setDay.set(year, month, day);
        if((year == 0 && month == 0 && day == 0) || (year != 0 && month != 0 && day != 0 && (setDay.before(currentDay) || setDay.equals(currentDay)))) {
            year = currentDay.get(Calendar.YEAR);
            month = currentDay.get(Calendar.MONTH);
            day = currentDay.get(Calendar.DAY_OF_MONTH);
        }

        setDay.set(year, month, day);
        if(setDay.after(currentDay)) {
            noneset = false;
            dayString = " " + String.valueOf(day) + "." + String.valueOf(month + 1) + "." + String.valueOf(year);
        }
        if(noneset){
            returnval=false;
        }
        showFilterActive(returnval, dayString);
        return returnval;
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        MenuItem filtericon=menu.findItem(R.id.actionb_filter_list);
        Drawable drawable;
        if(isFilterSet())
        {
            drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_filter_full);
        }
        else
        {
            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_filter);
        }
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(mContext, R.color.noecard_white));
        filtericon.setIcon(drawable);
    }
}
