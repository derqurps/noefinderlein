package at.qurps.noefinderlein.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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


import com.google.android.gms.location.DetectedActivity;

import org.joda.time.DateTime;
import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;


public class Activity_Main extends AppCompatActivity implements
            OnLocationUpdatedListener,
            NavigationView.OnNavigationItemSelectedListener,
            Fragment_LocationList.Callbacks,
            Fragment_LocationNear.Callbacks,
            Fragment_LocationFavorits.Callbacks{

    private static final String TAG = "Activity_Main";
    private SharedPreferences mPrefs;

    private SharedPreferences.Editor mEditor;
    public DestinationsDB db;
    public int mActiveyear;
    private boolean mTwoPane;
    public Location mLastLocation;

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    private LocationGooglePlayServicesProvider provider;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";


    public static final String KEY_LICENCE_ACCEPTED="licence_accepted_v2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This does nothing...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        this.db= new DestinationsDB(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        initYear();

        Resources res = getResources();
        mTwoPane = res.getBoolean(R.bool.has_two_panes);
        startDefaultScreen();

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
    /*@Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        setYearString();
    }*/
    @Override
    public void onLocationUpdated(Location location) {
        if(isBetterLocation(location,mLastLocation)) {
            //Log.d(TAG," better location found "+String.valueOf(mCurrentLocation.distanceTo(location)));
            mLastLocation = location;
            //Log.d(TAG," better location found "+String.valueOf(mCurrentLocation.getLatitude())+" " + String.valueOf(mCurrentLocation.getLongitude()));
            Intent data = new Intent("locationupdate");
            data.putExtra("key", "now");
            this.sendBroadcast(data);
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
        if (id == R.id.action_settings) {
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
        detailItemChosen(id, jahr, Fragment_LocationNear.TAG);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(id == R.id.nav_near){
            startLocationUpdates();
        }else{
            stopLocationUpdates();
        }
        if (id == R.id.nav_all) {
            fab.show();
        }else{
            fab.hide();
        }

        if (id == R.id.nav_all) {
            startDefaultScreen();
        } else if (id == R.id.nav_favorites) {
            startFavoritsScreen();
        } else if (id == R.id.nav_visited) {
            startVisitedScreen();
        } else if (id == R.id.nav_near) {
            startNearScreen();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Activity_Settings.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {

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
        Fragment_LocationList testfrag = (Fragment_LocationList) getSupportFragmentManager().findFragmentByTag(Fragment_LocationList.TAG);
        if(testfrag==null || !testfrag.isVisible()) {
            Bundle arguments = new Bundle();
            arguments.putInt(Fragment_LocationList.ARG_ITEM_JAHR, mActiveyear);
            arguments.putBoolean(Fragment_LocationList.ARG_MTWOPANE, mTwoPane);
            arguments.putBoolean(Fragment_LocationList.ARG_ISREGION, false);

            Fragment_LocationList fragment = new Fragment_LocationList();
            fragment.setArguments(arguments);
            FragmentManager man = getSupportFragmentManager();
            if ( man.getBackStackEntryCount() > 0) {
                Log.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_LocationList.TAG)
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

    private void detailItemChosen(int id, int year, String fragmentTAG) {
        int selectedid = id;
        Bundle arguments = new Bundle();
        arguments.putInt(Activity_Detail.ARG_ITEM_ID, selectedid);
        arguments.putInt(Activity_Detail.ARG_ITEM_JAHR, year);
        arguments.putBoolean(Activity_Detail.ARG_MTWOPANE, mTwoPane);
        Intent intent = new Intent(this, Activity_Detail.class);
        intent.putExtras(arguments);
        startActivity(intent);
        //finish();


    }

    public void initYear(){
        Boolean overwriteyear = mPrefs.getBoolean(Activity_Settings.KEY_PREF_OVERWRITE_YEAR, false);
        Integer year;
        if(overwriteyear){
            year = Integer.valueOf(mPrefs.getString(Activity_Settings.KEY_PREF_OVERWRITE_YEAR_MAN, "2014"));
            if(!(db.isYearInDatabase(year))){
                setToast(getResources().getString(R.string.error_year_not_found_in_database),1);
            }else{
                setYear(year);
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
    }
    public void setYearString(){
        setContentView(R.layout.activity_list);
        String year = ""+String.valueOf(mActiveyear)+"/"+String.valueOf(mActiveyear+1);
        TextView text = (TextView) this.findViewById(R.id.text_noecardyear);
        if(text != null){
            text.setText(year);
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

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();

        smartLocation.location(provider).start(this);
        setToast(getResources().getString(R.string.location_updated_started), 0);
    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        SmartLocation.with(this).location().stop();
        //setToast(getResources().getString(R.string.location_updated_stopped), 0);

    }
    protected void updateDB(){
        Log.d("Response1: ",String.valueOf(mActiveyear));
        Integer [] myTaskParams = { mActiveyear };
        Log.d("api path: ",String.valueOf(getResources().getString(R.string.api_path)));
        new Downloader_Destination(getApplicationContext()).execute(myTaskParams);
    }
    /*
     CALLBACK METHODS

     */
    @Override
    public void onItemSelected_Fragment_LocationList(int id,int year) {

        detailItemChosen(id, year, Fragment_LocationList.TAG);
    }
    @Override
    public void onItemSelected_Fragment_LocationFavorits(int id,int year) {

        detailItemChosen(id, year, Fragment_LocationList.TAG);
    }

}
