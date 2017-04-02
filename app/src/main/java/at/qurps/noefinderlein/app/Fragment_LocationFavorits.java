package at.qurps.noefinderlein.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 02.04.16.
 */
public class Fragment_LocationFavorits extends ListFragment {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    public static final String TAG = "Fragment_LocationFavori";
    public static final String ARG_ITEM_JAHR = "item_jahr" ;
    public static final String ARG_MTWOPANE ="mTwoPane" ;
    public static final String ARG_ISREGION ="isRegion" ;

    private Callbacks mCallbacks = sDummyCallbacks;
    private DestinationsDB db;
    private Context mContext;
    List<DB_Location_NoeC> listItems=new ArrayList<DB_Location_NoeC>();
    ArrayAdapter_Mainlist adapter;
    private boolean mTwoPane;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public boolean mIsRegion=false;
    public int mRegionItemJahr;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected_Fragment_LocationFavorits(int i,int year);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected_Fragment_LocationFavorits(int id, int year) {
        }
    };

    public Fragment_LocationFavorits() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext=getActivity();
        this.db= new DestinationsDB(mContext);
        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

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
    private void dbContentChanged(){
        listItems = db.getAllFavoritLocations(mRegionItemJahr);
        adapter=new ArrayAdapter_Mainlist(mContext,listItems);
        adapter.setCallingFragment(TAG);
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
    public void onResume() {
        super.onResume();
        dbContentChanged();
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
        mCallbacks.onItemSelected_Fragment_LocationFavorits(adapter.getIdtoPosition(position), adapter.getJahrtoPosition(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
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
}
