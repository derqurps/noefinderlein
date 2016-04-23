package at.qurps.noefinderlein.app;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;


public class Activity_Main extends AppCompatActivity implements
            ConnectionCallbacks,
            OnConnectionFailedListener,
            NavigationView.OnNavigationItemSelectedListener,
            Fragment_LocationList.Callbacks,
            Fragment_LocationNear.Callbacks,
            Fragment_LocationFavorits.Callbacks,
            Fragment_Regions.Callbacks,
            Downloader_Destination.Callbacks{

    private static final String TAG = "Activity_Main";
    private SharedPreferences mPrefs;

    private SharedPreferences.Editor mEditor;
    public DestinationsDB db;
    public int mActiveyear;
    private boolean mTwoPane;
    public Location mLastLocation;
    private boolean locationUpdatesRunning = false;
    private static final int LOCATION_REQUEST = 1;
    private static final int LAST_LOCATION_REQUEST = 2;

    protected LocationManager locationManager;
    private GoogleApiClient mGoogleApiClient;

    private TextView yeartext;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private static final int TWO_MINUTES = 1000 * 60 * 2;


    public static final String KEY_LICENCE_ACCEPTED="licence_accepted_v2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_list);

        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        if(nav_view!=null) {
            View headerLayout = nav_view.getHeaderView(0);
            nav_view.setNavigationItemSelectedListener(this);
            yeartext = (TextView) headerLayout.findViewById(R.id.text_noecardyear);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab!=null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "This does nothing...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
            fab.hide();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer!=null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }





        this.db= new DestinationsDB(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        initYear();

        Resources res = getResources();
        mTwoPane = res.getBoolean(R.bool.has_two_panes);
        startDefaultScreen();

        ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun()) {
            cl.getFullLogDialog().show();
        }

        if (!mPrefs.getBoolean(KEY_LICENCE_ACCEPTED, false)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_report_problem);
            builder.setTitle(R.string.disclaimer_title);
            builder.setMessage(R.string.disclaimer);
            //builder.setInverseBackgroundForced(true);
            builder.setPositiveButton(R.string.disclaimer_button_agree, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mEditor.putBoolean(KEY_LICENCE_ACCEPTED, true);
                    mEditor.commit();
                    updateDB();
                }
            });
            builder.setNegativeButton(R.string.disclaimer_button_disagree, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }else{
            updateDB();
        }



    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        initYear();
        onDownloadCompleted();
    }
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    protected final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void locationChanged(Location location){
        if(location != null) {

            if (isBetterLocation(location, mLastLocation)) {
                //Log.d(TAG," better location found "+String.valueOf(mCurrentLocation.distanceTo(location)));
                mLastLocation = location;
                //Log.d(TAG," better location found "+String.valueOf(mCurrentLocation.getLatitude())+" " + String.valueOf(mCurrentLocation.getLongitude()));
                Intent data = new Intent("locationupdate");
                data.putExtra("key", "now");
                data.putExtra("lat", location.getLatitude());
                data.putExtra("lon", location.getLongitude());
                broadcastUpdate(data);
            }
        }else{
            Util.setToast(this, "Location null", 1);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Activity_Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link Fragment_LocationNear.Callbacks} indicating
     * that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected_Fragment_LocationNear(int id,int jahr) {
        detailItemChosen(id, jahr);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(id == R.id.nav_near){
            getLastKnownLocation();
            //startLocationUpdates();
        }else{
            stopLocationUpdates();
        }
        if (id == R.id.nav_all) {
            //fab.show();
        }else{
            fab.hide();
        }

        if (id == R.id.nav_all) {
            startDefaultScreen();
        } else if (id == R.id.nav_region) {
            startRegionsScreen();
        } else if (id == R.id.nav_favorites) {
            startFavoritsScreen();
        } else if (id == R.id.nav_visited) {
            startVisitedScreen();
        } else if (id == R.id.nav_near) {
            startNearScreen();
        } else if (id == R.id.nav_money) {
            String url = String.valueOf("https://noecard.reitschmied.at/donate");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Activity_Settings.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, Activity_About.class);
            startActivity(intent);
        } /*else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if(location == null){
            return false;
        }
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        try {
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
        }catch(Exception e){}
        return false;
    }
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void startDefaultScreen(){
        startDefaultScreen(false,0,"", true);
    }
    private void startDefaultScreen(boolean isRegion, int regionId, String name, boolean forcereplace){
        Fragment_LocationList testfrag = (Fragment_LocationList) getSupportFragmentManager().findFragmentByTag(Fragment_LocationList.TAG);
        Fragment_Regions testReg = (Fragment_Regions) getSupportFragmentManager().findFragmentByTag(Fragment_Regions.TAG);
        if(testfrag==null || !testfrag.isVisible() || (forcereplace && name.equals(""))) {
            Bundle arguments = new Bundle();
            arguments.putInt(Fragment_LocationList.ARG_ITEM_JAHR, mActiveyear);
            arguments.putBoolean(Fragment_LocationList.ARG_MTWOPANE, mTwoPane);
            arguments.putBoolean(Fragment_LocationList.ARG_ISREGION, isRegion);
            arguments.putInt(Fragment_LocationList.ARG_REGION, regionId);
            arguments.putString(Fragment_LocationList.ARG_REGION_NAME, name);

            Fragment_LocationList fragment = new Fragment_LocationList();
            fragment.setArguments(arguments);
            FragmentManager man = getSupportFragmentManager();

            if (man.getBackStackEntryCount() > 0) {
                Log.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }

            man.beginTransaction().remove(fragment).commit();
            FragmentTransaction transABla = man.beginTransaction();
            transABla.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
            transABla.replace(R.id.region_list_container, fragment, Fragment_LocationList.TAG);
            transABla.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if(testReg != null && testReg.isVisible()) {
                transABla.addToBackStack(Fragment_Regions.TAG);
            }
            transABla.commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                Log.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    Log.d(TAG," lalala----");
                    onItemSelected(0);
                }*/
        }
    }
    private void startRegionsScreen(){
        Fragment_Regions testfrag = (Fragment_Regions) getSupportFragmentManager().findFragmentByTag(Fragment_Regions.TAG);
        if(testfrag==null || !testfrag.isVisible()) {
            Bundle arguments = new Bundle();
            arguments.putInt(Fragment_Regions.ARG_ITEM_JAHR, mActiveyear);
            arguments.putBoolean(Fragment_Regions.ARG_MTWOPANE, mTwoPane);

            Fragment_Regions fragment = new Fragment_Regions();
            fragment.setArguments(arguments);
            FragmentManager man = getSupportFragmentManager();
            if ( man.getBackStackEntryCount() > 0) {
                Log.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_Regions.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                Log.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    Log.d(TAG," lalala----");
                    onItemSelected(0);
                }*/
        }
    }
    private void startNearScreen() {
        Fragment_LocationNear testfrag = (Fragment_LocationNear) getSupportFragmentManager().findFragmentByTag(Fragment_LocationNear.TAG);
        if(testfrag==null || !testfrag.isVisible()) {
            Bundle arguments = new Bundle();
            arguments.putInt(Fragment_LocationNear.ARG_ITEM_JAHR, mActiveyear);
            arguments.putBoolean(Fragment_LocationNear.ARG_MTWOPANE, mTwoPane);
            arguments.putBoolean(Fragment_LocationNear.ARG_ISREGION, false);

            Fragment_LocationNear fragment = new Fragment_LocationNear();
            fragment.setArguments(arguments);
            FragmentManager man = getSupportFragmentManager();
            if ( man.getBackStackEntryCount() > 0) {
                Log.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_LocationNear.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                Log.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    Log.d(TAG," lalala----");
                    onItemSelected(0);
                }*/
        }
    }
    private void startFavoritsScreen() {
        Fragment_LocationFavorits testfrag = (Fragment_LocationFavorits) getSupportFragmentManager().findFragmentByTag(Fragment_LocationFavorits.TAG);
        if(testfrag==null || !testfrag.isVisible()) {
            Bundle arguments = new Bundle();
            arguments.putInt(Fragment_LocationFavorits.ARG_ITEM_JAHR, mActiveyear);
            arguments.putBoolean(Fragment_LocationFavorits.ARG_MTWOPANE, mTwoPane);
            arguments.putBoolean(Fragment_LocationFavorits.ARG_ISREGION, false);

            Fragment_LocationFavorits fragment = new Fragment_LocationFavorits();
            fragment.setArguments(arguments);
            FragmentManager man = getSupportFragmentManager();
            if ( man.getBackStackEntryCount() > 0) {
                Log.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_LocationFavorits.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                Log.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    Log.d(TAG," lalala----");
                    onItemSelected(0);
                }*/
        }
    }
    private void startVisitedScreen() {
        Fragment_LocationVisited testfrag = (Fragment_LocationVisited) getSupportFragmentManager().findFragmentByTag(Fragment_LocationVisited.TAG);
        if(testfrag==null || !testfrag.isVisible()) {
            Bundle arguments = new Bundle();
            arguments.putInt(Fragment_LocationVisited.ARG_ITEM_JAHR, mActiveyear);
            arguments.putBoolean(Fragment_LocationVisited.ARG_MTWOPANE, mTwoPane);
            arguments.putBoolean(Fragment_LocationVisited.ARG_ISREGION, false);

            Fragment_LocationVisited fragment = new Fragment_LocationVisited();
            fragment.setArguments(arguments);
            FragmentManager man = getSupportFragmentManager();
            if ( man.getBackStackEntryCount() > 0) {
                Log.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_LocationVisited.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                Log.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    Log.d(TAG," lalala----");
                    onItemSelected(0);
                }*/
        }
    }
    private void detailItemChosen(int id, int year){
        detailItemChosen(id, year, this);
    }
    public static void detailItemChosen(int id, int year, Context mContext) {

        Bundle arguments = new Bundle();
        arguments.putInt(Activity_Detail.ARG_ITEM_ID, id);
        arguments.putInt(Activity_Detail.ARG_ITEM_JAHR, year);
        //arguments.putBoolean(Activity_Detail.ARG_MTWOPANE, mTwoPane);
        Intent intent = new Intent(mContext, Activity_Detail.class);
        intent.putExtras(arguments);
        mContext.startActivity(intent);
        //finish();


    }

    public void initYear(){
        Boolean overwriteyear = mPrefs.getBoolean(Activity_Settings.KEY_PREF_OVERWRITE_YEAR, false);
        Integer year;
        if(overwriteyear){
            year = Integer.valueOf(mPrefs.getString(Activity_Settings.KEY_PREF_OVERWRITE_YEAR_MAN, "2014"));
            Log.d(TAG, String.valueOf(year));
            setYear(year);
            if(!(db.isYearInDatabase(year))){
                setToast(getResources().getString(R.string.error_year_not_found_in_database),1);
                updateDB();
            }
        }else{
            DateTime dt = new DateTime();
            year = dt.getYear();
            Log.d(TAG, String.valueOf(year));
            DateTime endOfMarch = new DateTime(year, 3, 31, 1, 1);
            if(!dt.isAfter(endOfMarch)){
                year = year-1;
            }
            Log.d(TAG, String.valueOf(year));
            setYear(year);
            if(!(db.isYearInDatabase(year))){
                setToast(getResources().getString(R.string.error_year_not_found_in_database),1);
            }

        }
    }
    public int getYear(){
        return mActiveyear;
    }
    public void setYear(int year){
        mActiveyear=year;
        setYearString();
    }
    public void setYearString(){

        String year = ""+String.valueOf(mActiveyear)+"/"+String.valueOf(mActiveyear+1);
        if(yeartext != null){
            yeartext.setText(year);
        }
    }

    public void setToast(final String message,final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (length == 1) {
                    Toast.makeText(getApplicationContext(), message,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), message,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public DestinationsDB getDb() {
        return this.db;
    }


    protected void getLastKnownLocation(){
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            if(ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(Activity_Main.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LAST_LOCATION_REQUEST);
                return;
            }
            Location loc = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            locationChanged(loc);
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Activity_Main.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        if (locationManager != null) {
            final int minTime = 10*1000;
            final int minDistance = 0;
            final Criteria criteria = new Criteria();
            String provider;
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            provider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
            locationUpdatesRunning = true;
        }
        setToast(getResources().getString(R.string.location_updated_started), 0);
    }

    protected void stopLocationUpdates() {

        if(locationUpdatesRunning) {
            if (ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(Activity_Main.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                return;
            }
            if (locationManager != null) {
                locationManager.removeUpdates(listener);
            }
            locationUpdatesRunning = false;
        }
    }
    protected void broadcastUpdate(Intent data){
        this.sendBroadcast(data);
    }
    protected void updateDB(){
        updateDB(false);
    }
    protected void updateDB(boolean force){
        Boolean offlinemode = mPrefs.getBoolean(Activity_Settings.KEY_PREF_OFFLINE_MODE, false);
        if(!offlinemode) {
            Log.d("Response1: ", String.valueOf(mActiveyear));
            Integer[] myTaskParams = {mActiveyear};
            Log.d("api path: ", String.valueOf(getResources().getString(R.string.api_path)));
            new Downloader_Destination(getApplicationContext(), this).execute(myTaskParams);
        }else{
            Util.setToast(this, getString(R.string.toast_offline),0);
        }
    }
    /*
     CALLBACK METHODS

     */
    @Override
    public void onItemSelected_Fragment_LocationList(int id,int year) {

        detailItemChosen(id, year);
    }
    @Override
    public void onItemSelected_Fragment_LocationFavorits(int id,int year) {

        detailItemChosen(id, year);
    }
    @Override
    public void onDownloadCompleted() {
        System.gc();
        //Util.setToast(this,"Download finished",1);
        Intent data = new Intent("dataupdate");
        data.putExtra("key", "now");
        data.putExtra("year", mActiveyear);
        this.sendBroadcast(data);
    }
    @Override
    public void onDownloadCompleted(int id) {

        /*Intent data = new Intent("dataupdate");
        data.putExtra("key", "now");
        data.putExtra("id", id);
        this.sendBroadcast(data);*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case LOCATION_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationUpdates();
                }else{
                    Util.setToast(this, "Location updates denied", 1);
                    //startDefaultScreen();
                }
                break;
            case LAST_LOCATION_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLastKnownLocation();
                }else{
                    Util.setToast(this, "Location updates denied", 1);
                    //startDefaultScreen();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRegionSelected(int i, String name) {
        startDefaultScreen(true, i, name, false);
    }
}
