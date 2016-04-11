package at.qurps.noefinderlein.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Activity_Map extends FragmentActivity implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Location_NoeC>,
        ClusterManager.OnClusterInfoWindowClickListener<Location_NoeC>,
        ClusterManager.OnClusterItemClickListener<Location_NoeC>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Location_NoeC> {

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
    ArrayList<Location_NoeC> inputPoints;
    private ClusterManager<Location_NoeC> mClusterManager;
    private String mItemIds;
    private int mYear;

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

                mItemIds = bundle.getString(ARG_ITEM_IDS);
                mYear = bundle.getInt(ARG_ITEM_YEAR);
            }
            else
            {
                Toast.makeText(mContext, "Destination number not found...", Toast.LENGTH_SHORT).show();
            }
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
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_noe_marker)));
            }
            else
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NOEMITTE, 8));
                mClusterManager = new ClusterManager<Location_NoeC>(mContext, mMap);
                mClusterManager.setRenderer(new LocationRenderer());
                mMap.setOnCameraChangeListener(mClusterManager);
                mMap.setOnMarkerClickListener(mClusterManager);
                mMap.setOnInfoWindowClickListener(mClusterManager);
                mClusterManager.setOnClusterClickListener(this);
                mClusterManager.setOnClusterInfoWindowClickListener(this);
                mClusterManager.setOnClusterItemClickListener(this);
                mClusterManager.setOnClusterItemInfoWindowClickListener(this);

                addItems();
                mClusterManager.cluster();
            }
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Location_NoeC> cluster) {
        String firstName = cluster.getItems().iterator().next().getName();
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();
        return true;
    }
    @Override
    public void onClusterInfoWindowClick(Cluster<Location_NoeC> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Location_NoeC item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Location_NoeC item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    private void addItems(){
        List<Location_NoeC> ziele;
        if(mItemIds==null)
        {
            ziele=this.db.getAllLocations(mYear);
        }
        else
        {
            ziele=this.db.getAllLocations_toDestIDs(mItemIds, mYear);
        }
        for(int i=0;i<ziele.size();i++)
        {
            mClusterManager.addItem((Location_NoeC)ziele.get(i));
        }
        //mClusterManager.addItems(this.inputPoints);
    }

    private class LocationRenderer extends DefaultClusterRenderer<Location_NoeC> {
        /*private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;*/
        public LocationRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.map_item, null);
            /*mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) 50;
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) 2;
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);*/

        }
        @Override
        protected void onBeforeClusterItemRendered(Location_NoeC location, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            /*mImageView.setImageResource(person.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();*/
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_noe_marker))
                    .title(location.getName())
                    .snippet(location.getBeschreibung());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Location_NoeC> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            /*List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getResources().getDrawable(p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));*/

            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_noe_multimarker));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }
}
