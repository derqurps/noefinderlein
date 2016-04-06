package at.qurps.noefinderlein.app;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class Activity_Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = "Activity_Map";

    public static final String ARG_ITEM_ID = "item_id" ;
    public static final String ARG_ITEM_IDS = "item_ids" ;
    public static final String ARG_ITEM_YEAR = "item_jahr" ;
    public static final String ARG_MTWOPANE ="mTwoPane" ;
    static final LatLng NOEMITTE = new LatLng(48.193557, 15.646935);
    private DestinationsDB db;
    private Location_NoeC ziel=null;
    private Context mContext;
    ArrayList<MyItem> inputPoints;
    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        this.db= new DestinationsDB(mContext);
        Bundle bundle = getIntent().getExtras();

        if (bundle!=null) {
            if (bundle.containsKey(ARG_ITEM_ID) && bundle.containsKey(ARG_ITEM_YEAR))
            {
                ziel = db.getLocationToId(bundle.getInt(ARG_ITEM_ID));
            }
            else if(bundle.containsKey(ARG_ITEM_IDS) && bundle.containsKey(ARG_ITEM_YEAR)) {
                buildInputPoints(
                        bundle.getString(ARG_ITEM_IDS),
                        bundle.getInt(ARG_ITEM_YEAR));
            }
            else
            {
                Toast.makeText(mContext, "Destination number not found...", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            buildInputPoints();
        }
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        if (mMap != null) {
            // The Map is verified. It is now safe to manipulate the map.

            //mMap.setMyLocationEnabled(true);

            if (ziel!=null) {
                Log.d("ACMD1: ", String.valueOf(ziel.getLatitude())+" "+String.valueOf(ziel.getLongitude()));
                LatLng ziellocation = new LatLng(ziel.getLatitude(), ziel.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ziellocation, 14));
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(ziellocation)
                        .title(ziel.getName())
                        .snippet(ziel.getBeschreibung())
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.mipmap.ic_noe_marker)));
            }
            else
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NOEMITTE, 8));
                mClusterManager = new ClusterManager<MyItem>(mContext, mMap);
                mMap.setOnCameraChangeListener(mClusterManager);
                mMap.setOnMarkerClickListener(mClusterManager);

                initClusterPoints();
            }
        }
    }



    private void buildInputPoints()
    {
        buildInputPoints(null, 0);
    }
    private void buildInputPoints(String ItemList, int year) {
        List<Location_NoeC> ziele;
        if(ItemList==null)
        {
            ziele=this.db.getAllLocations(year);
        }
        else
        {
            ziele=this.db.getAllLocations_toDestIDs(ItemList,year);
        }
        this.inputPoints = new ArrayList<MyItem>(ziele.size());
        for(int i=0;i<ziele.size();i++)
        {
            this.inputPoints.add(new MyItem(new LatLng(ziele.get(i).getLatitude(), ziele.get(i).getLongitude()),ziele.get(i) ));
        }
    }
    private void initClusterPoints() {
        if (this.mMap != null && this.inputPoints != null && this.inputPoints.size() > 0) {

            this.mClusterManager.addItems(inputPoints);

        }
    }

    private class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private Location_NoeC info;

        public MyItem(LatLng latlng, Location_NoeC infos) {
            mPosition = latlng;
            info = infos;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }
}
