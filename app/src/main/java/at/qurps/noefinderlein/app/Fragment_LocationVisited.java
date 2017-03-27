package at.qurps.noefinderlein.app;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 02.04.16.
 */
public class Fragment_LocationVisited extends ListFragment {
    public static final String TAG = "Fragment_LocationVisite";
    public static final String ARG_ITEM_JAHR = "item_jahr" ;
    public static final String ARG_MTWOPANE ="mTwoPane" ;
    public static final String ARG_ISREGION ="isRegion" ;
    private static final String STATE_ACTIVATED_POSITION = "activated_position";


    private Callbacks mCallbacks = sDummyCallbacks;



    private DestinationsDB db;
    private Context mContext;
    List<DB_Location_NoeC> listItems=new ArrayList<DB_Location_NoeC>();
    ArrayAdapter_Visited adapter;
    private boolean mTwoPane;
    private Menu mMenu;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public boolean mIsRegion=false;
    public int mRegionItemJahr;
    private View rootView;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected_Fragment_LocationVisited(int i, int year);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected_Fragment_LocationVisited(int id, int year) {
        }
    };

    public Fragment_LocationVisited() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext=getActivity();
        this.db= new DestinationsDB(mContext);
        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);
        if (mMenu != null) {
            onPrepareOptionsMenu(mMenu);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_location_visited, container, false);
        //container.removeAllViews();

        parseArguments(getArguments());


        updateList();

        return rootView;
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
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
                final int subpos = position;
                AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
                ad.setCancelable(false);
                ad.setTitle(mContext.getString(R.string.deleteVisited));
                ad.setPositiveButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        db.removeVisited(adapter.getVIdtoPosition(subpos));
                        updateList();
                    }
                });
                ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                ad.create().show();

                return true;
            }
        });
    }


    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_visited, menu);

    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mMenu=menu;
    }
    @Override
    public void onListItemClick(ListView listView, View view, int position,
                                long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected_Fragment_LocationVisited(adapter.getIdtoPosition(position), adapter.getJahrtoPosition(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }
    public void updateList() {
        listItems = db.getAllVisitedLocations(mRegionItemJahr);
        adapter=new ArrayAdapter_Visited(getActivity(),listItems);
        setListAdapter(adapter);
        TextView ersparnis = (TextView) rootView.findViewById(R.id.ersparnis_summe);

        ersparnis.setText(adapter.getGesamtErsparnis());
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
    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

}
