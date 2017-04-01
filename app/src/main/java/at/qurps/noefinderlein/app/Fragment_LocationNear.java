package at.qurps.noefinderlein.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by roman on 02.04.16.
 */
public class Fragment_LocationNear extends ListFragment {

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
    private Callbacks mCallbacks = sDummyCallbacks;
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
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected_Fragment_LocationNear(int id, int jahr) {
        }
    };
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
        View rootView = inflater.inflate(R.layout.fragment_location_list, container, false);
        //container.removeAllViews();

        parseArguments(getArguments());

        dbContentChanged();

        return rootView;
    }
    public void dbContentChanged() {
        listItems = db.getAllMenuDistanceLocations(mRegionItemJahr);
        adapter=new ArrayAdapter_Near(mContext,listItems,null);
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

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
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

}
