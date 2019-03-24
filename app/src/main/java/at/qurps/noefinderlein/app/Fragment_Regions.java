package at.qurps.noefinderlein.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 16.04.16.
 */
public class Fragment_Regions extends ListFragment {
    public static final String TAG = "Fragment_Region";
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    public static final String ARG_ITEM_JAHR = "item_jahr" ;
    public static final String ARG_MTWOPANE ="mTwoPane" ;

    private int mActivatedPosition = ListView.INVALID_POSITION;

    private Callbacks mCallbacks;
    private DestinationsDB db;
    private Context mContext;
    private boolean mTwoPane;
    private Bundle msavedInstanceState;
    private View rootView;
    List<Region_Picture_and_Text> listItems=new ArrayList<Region_Picture_and_Text>();
    ArrayAdapter_Region adapter;

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onRegionSelected(int i, String name);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext=getActivity();
        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);
        this.db= new DestinationsDB(mContext);

        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_waldviertel), R.mipmap.liste_region_waldviertel, 41)); // 1535
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_weinviertel), R.mipmap.liste_region_weinviertel, 38)); // 1536
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_donau_noe), R.mipmap.liste_region_donau_noe, 28)); // 1533
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_mostviertel), R.mipmap.liste_region_mostviertel, 43)); // 1534
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_wienerwald), R.mipmap.liste_region_wienerwald, 32)); // 1538
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_wiener_alpen), R.mipmap.liste_region_wiener_alpen, 46)); // 1537
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_wien), R.mipmap.liste_region_wien, 85)); // 1539
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_burgenland), R.mipmap.liste_region_burgenland, 84)); // 1541
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_oberoesterreich), R.mipmap.liste_region_oberoesterreich, 87)); // 1591
        listItems.add(new Region_Picture_and_Text(getString(R.string.region_list_steiermark), R.mipmap.liste_region_steiermark, 86)); // 1540
        adapter = new ArrayAdapter_Region(getActivity(), listItems);
        setListAdapter(adapter);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_location_list, container, false);

        return rootView;
    }
    @Override
    public void onListItemClick(ListView listView, View view, int position,
                                long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onRegionSelected(adapter.getNumbertoPosition(position),adapter.getNametoPosition(position));
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
}
