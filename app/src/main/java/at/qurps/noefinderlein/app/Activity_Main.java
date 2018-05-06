package at.qurps.noefinderlein.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hypertrack.hyperlog.HyperLog;
// import com.squareup.leakcanary.LeakCanary;

import java.util.Calendar;


public class Activity_Main extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        Fragment_LocationList.Callbacks,
        Fragment_LocationNear.Callbacks,
        Fragment_LocationFavorits.Callbacks,
        Fragment_LocationVisited.Callbacks,
        Fragment_Regions.Callbacks,
        Downloader_Destination_v2.Callbacks {

    private static final String TAG = "Activity_Main";
    public static final String KEY_LICENCE_ACCEPTED = "licence_accepted_v3";
    public static final String KEY_GAME_SIGN_IN_CLICKED = "game_sign_in_clicked_v2";
    public static final String REQUESTING_LOCATION_UPDATES_KEY = "requestLocationUpdateKey";

    private SharedPreferences mPrefs;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    private SharedPreferences.Editor mEditor;
    public DestinationsDB db;
    public int mActiveyear;
    private boolean mTwoPane;
    public Location mLastLocation;

    private String serverAuthCode;

    private boolean mDownloadDone = false;
    private static final int LOCATION_REQUEST = 1;
    private static final int LAST_LOCATION_REQUEST = 2;

    // protected LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationClient;

    //private GoogleApiClient mGoogleApiClient;

    private TextView yeartext;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private static final int TWO_MINUTES = 1000 * 60 * 2;



    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_ACHIEVEMENT_UI = 9003;
    private static final int RC_LEADERBOARD_UI = 9004;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mGameSignInClicked = false;
    private int mGameSignInTry = 0;
    private boolean mRequestingLocationUpdates = false;

    private NavigationView nav_view;
    private FrameLayout mainView;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        //JodaTimeAndroid.init(this);
        if (savedInstanceState != null && savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mainView = findViewById(R.id.region_list_container);

        nav_view = findViewById(R.id.nav_view);

        if(nav_view!=null) {
            View headerLayout = nav_view.getHeaderView(0);
            nav_view.setNavigationItemSelectedListener(this);
            yeartext = headerLayout.findViewById(R.id.text_noecardyear);
        }
        mGameSignInTry = 0;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mGameSignInClicked = Util.getPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, false);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);




        FloatingActionButton fab = findViewById(R.id.fab);
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

        mDrawer = findViewById(R.id.drawer_layout);
        if(mDrawer!=null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            mDrawer.setDrawerListener(toggle);
            toggle.syncState();
        }





        this.db= new DestinationsDB(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        Boolean opendata = mPrefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, true);
        mEditor.putBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, opendata);
        mEditor.apply();

        Boolean logginEnabled = mPrefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_LOGGING, false);

        HyperLog.deleteLogs();
        if (logginEnabled) {
            HyperLog.initialize(this);
            HyperLog.setLogLevel(Log.VERBOSE);
        }

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


        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(getApplication());*/
    }

    /*private void buildGoogleApiClient() {
        buildGoogleApiClient(false);
    }
    private void buildGoogleApiClient(boolean reconnect) {

        // mGameSignInClicked = Util.getPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, false);

        if(mGoogleApiClient == null || reconnect) {
            GoogleApiClient.Builder mGoogleApiClientBuilder = new GoogleApiClient.Builder(this);
            //mGoogleApiClientBuilder.addConnectionCallbacks(this);
            mGoogleApiClientBuilder.addOnConnectionFailedListener(this);
            mGoogleApiClientBuilder.addApi(LocationServices.API);
            mGoogleApiClientBuilder.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            mGoogleApiClientBuilder.setViewForPopups(mainView);
            mGoogleApiClient = mGoogleApiClientBuilder.build();

        }
    }*/
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        initYear();
        notifyDataChanged();
        if (mGameSignInClicked) {
            signInSilently();
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        /*if(mDownloadDone && !isGameSignedIn()) {
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }*/
    }
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        /*if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }*/
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        // ...
        super.onSaveInstanceState(outState);
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
            int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackEntryCount == 0) {
                if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.backButtonExit), Toast.LENGTH_SHORT).show();
                }

                mBackPressed = System.currentTimeMillis();
            } else {
                super.onBackPressed();
                return;
            }
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
        } else if (id == R.id.game_login) {
            HyperLog.d(TAG, "Sign-in button clicked");

            mGameSignInTry = 0;
            mGameSignInClicked = true;
            Util.setPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, mGameSignInClicked);
            startSignInIntent();

        } else if (id == R.id.game_logout) {

            mGameSignInClicked = false;
            Util.setPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, mGameSignInClicked);
            setToast(getResources().getString(R.string.game_loggedout),1);
            signOut();

        } else if (id == R.id.game_achievements) {
            onShowAchievementsRequested();
        } else if (id == R.id.game_leaderboard) {
            onShowLeaderboardsRequested();
        } else if (id == R.id.nav_money) {
            String url = String.valueOf("https://noecard.reitschmied.at/donate");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Activity_Settings.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, Activity_About.class);
            intent.putExtra(Activity_About.KEY_YEAR, mActiveyear);
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
                HyperLog.d(TAG, String.valueOf(man.getBackStackEntryCount()));
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
                HyperLog.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    HyperLog.d(TAG," lalala----");
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
                HyperLog.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_Regions.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                HyperLog.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    HyperLog.d(TAG," lalala----");
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
                HyperLog.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_LocationNear.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                HyperLog.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    HyperLog.d(TAG," lalala----");
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
                HyperLog.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_LocationFavorits.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                HyperLog.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    HyperLog.d(TAG," lalala----");
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
                HyperLog.d(TAG, String.valueOf(man.getBackStackEntryCount()));
                man.popBackStack(man.getBackStackEntryAt(0).getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
            man.beginTransaction().remove(fragment).commit();
            man.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.region_list_container, fragment, Fragment_LocationVisited.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
                /*LocationListFragment existingfragment = (LocationListFragment) man.findFragmentByTag(LocationListFragment.TAG);
                HyperLog.d(TAG,"----"+String.valueOf(mTwoPane)+" "+String.valueOf(existingfragment));
                if(mTwoPane && (existingfragment==null || (existingfragment.mcontainer.getId()!=R.id.region_detail_container && ! existingfragment.isVisible()))){
                    HyperLog.d(TAG," lalala----");
                    onItemSelected(0);
                }*/
        }
    }
    private void detailItemChosen(int id, int year) {
        detailItemChosen(id, year, this);
    }
    public void detailItemChosen(int id, int year, Context mContext) {

        Bundle arguments = new Bundle();
        arguments.putInt(Activity_Detail.ARG_ITEM_ID, id);
        arguments.putInt(Activity_Detail.ARG_ITEM_JAHR, year);

        Intent intent = new Intent(mContext, Activity_Detail.class);
        intent.putExtras(arguments);
        mContext.startActivity(intent);
    }


    public void initYear(){
        Boolean overwriteyear = mPrefs.getBoolean(Activity_Settings.KEY_PREF_OVERWRITE_YEAR, false);
        Integer year;
        if(overwriteyear) {
            year = Integer.valueOf(mPrefs.getString(Activity_Settings.KEY_PREF_OVERWRITE_YEAR_MAN, "2014"));
            HyperLog.d(TAG, String.valueOf(year));
            setYear(year);
            if(!(db.isYearInDatabase(year))) {
                setToast(getResources().getString(R.string.error_year_not_found_in_database),1);
                updateDB();
            }
        } else {
            Calendar currentDay = Calendar.getInstance();
            year = currentDay.get(Calendar.YEAR);
            HyperLog.d(TAG, String.valueOf(year));
            Calendar endOfMarch = Calendar.getInstance();
            endOfMarch.set(year, 2, 31);

            HyperLog.d(TAG, String.valueOf(endOfMarch) + String.valueOf(currentDay));

            if(currentDay.before(endOfMarch)){
                year = year-1;
            }
            HyperLog.d(TAG, String.valueOf(year));
            setYear(year);
            if(!(db.isYearInDatabase(year))){
                setToast(getResources().getString(R.string.error_year_not_found_in_database),1);
            }

        }
    }
    public int getYear() {
        return mActiveyear;
    }
    public void setYear(int year) {
        mActiveyear=year;
        setYearString();
    }
    public void setYearString() {

        String year = "" + String.valueOf(mActiveyear) + "/" + String.valueOf(mActiveyear+1);
        if(yeartext != null){
            yeartext.setText(year);
        }
    }

    public void setToast(final String message, final int length) {
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
        if(ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Activity_Main.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LAST_LOCATION_REQUEST);
            return;
        }
        mFusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        locationChanged(location);
                        startLocationUpdates();
                    }
                }
            });
    }

    protected void startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Activity_Main.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        mRequestingLocationUpdates = true;
        LocationRequest request = LocationRequest.create();
        request.setSmallestDisplacement(20);
        request.setInterval(2000);
        mFusedLocationClient.requestLocationUpdates(request,
            new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    // setToast("gotLocation", 0);
                    for (Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        // ...
                        locationChanged(location);
                        mRequestingLocationUpdates = false;
                    }
                };
            },
        null /* Looper */);

        //setToast(getResources().getString(R.string.location_updated_started), 0);
    }

    protected void stopLocationUpdates() {

        if(mRequestingLocationUpdates) {
            if (ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(Activity_Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(Activity_Main.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                return;
            }
            mFusedLocationClient.removeLocationUpdates(new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    mRequestingLocationUpdates = false;
                };
            });

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
        Boolean wlanmode = mPrefs.getBoolean(Activity_Settings.KEY_PREF_WLAN_MODE, false);
        if(!offlinemode) {
            if (!wlanmode || (wlanmode && checkWifiOnAndConnected())) {
                HyperLog.d("Response1: ", String.valueOf(mActiveyear));
                Integer[] myTaskParams = {mActiveyear};
                HyperLog.d("api path: ", String.valueOf(getResources().getString(R.string.api_path)));
                new Downloader_Destination_v2(getApplicationContext(), this).execute(myTaskParams);
            }
        }else{
            Util.setToast(this, getString(R.string.toast_offline),0);
        }
    }
    private boolean checkWifiOnAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
        } else {
            return true;
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
    public void onItemSelected_Fragment_LocationVisited(int id, int year) {
        HyperLog.d(TAG, String.valueOf(id) + ' ' + String.valueOf(year));
        detailItemChosen(id, year);
    }
    @Override
    public void onDownloadCompleted() {
        notifyDataChanged();
        /*if(mGoogleApiClient!= null && !mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }*/
    }
    public void notifyDataChanged() {
        System.gc();
        //Util.setToast(this,"Download finished",1);
        Intent data = new Intent("dataupdate");
        data.putExtra("key", "now");
        data.putExtra("year", mActiveyear);
        mDownloadDone=true;
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
    /*@Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        HyperLog.d(TAG, "onConnectionFailed() called, result: " + result);
        if (mResolvingConnectionFailure) {
            HyperLog.d(TAG, "onConnectionFailed(): already resolving");
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mGameSignInClicked || mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mGameSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, result,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
        isGameSignedIn();
    }*/


    /*@Override
    public void onConnectionSuspended(int cause) {
        HyperLog.d(TAG, "onConnectionSuspended() called: " + cause);
        if(mDownloadDone) {
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }*/

    /*@Override
    public void onConnected(Bundle connectionHint) {
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
        HyperLog.d(TAG, "onconnected");

        if(isGameSignedIn()){
            HyperLog.d(TAG, "Connected successful to games");
        }
    }*/


    @Override
    public void onRegionSelected(int i, String name) {
        startDefaultScreen(true, i, name, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            HyperLog.d(TAG, "onActivityResult RC_SIGN_IN, responseCode="
                    + resultCode + ", intent=" + data);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                GamesClient gamesClient = Games.getGamesClient(Activity_Main.this, signedInAccount);
                gamesClient.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                gamesClient.setViewForPopups(mainView);
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
            isGameSignedIn();
        }
    }
    private boolean isGameSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }
    public void onShowAchievementsRequested() {
        if (isGameSignedIn()) {
            Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                        }
                    });
        } else {
            setToast(getString(R.string.achievements_not_available), 2);
        }
    }



    public void onShowLeaderboardsRequested() {
        if (isGameSignedIn()) {
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .getAllLeaderboardsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, RC_LEADERBOARD_UI);
                        }
                    });
        } else {
            setToast(getString(R.string.leaderboards_not_available), 2);
        }
    }
    private void showGameLogin() {
        Menu mainmenu = nav_view.getMenu();
        mainmenu.findItem(R.id.game_login).setVisible(true);
        mainmenu.findItem(R.id.game_logout).setVisible(false);
        mainmenu.findItem(R.id.game_achievements).setVisible(false);
        mainmenu.findItem(R.id.game_leaderboard).setVisible(false);
    }
    private void showGameLogout() {
        Menu mainmenu = nav_view.getMenu();
        mainmenu.findItem(R.id.game_login).setVisible(false);
        mainmenu.findItem(R.id.game_logout).setVisible(true);
        mainmenu.findItem(R.id.game_achievements).setVisible(true);
        mainmenu.findItem(R.id.game_leaderboard).setVisible(true);
    }



    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
            new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) {
                        // The signed in account is stored in the task's result.
                        GoogleSignInAccount signedInAccount = task.getResult();

                        GamesClient gamesClient = Games.getGamesClient(Activity_Main.this, signedInAccount);
                        gamesClient.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                        gamesClient.setViewForPopups(mainView);
                        showGameLogout();
                    } else {
                        mGameSignInTry++;
                        if (mGameSignInTry > 0) {
                            mGameSignInClicked = false;
                            Util.setPreferencesBoolean(Activity_Main.this, KEY_GAME_SIGN_IN_CLICKED, mGameSignInClicked);
                        } else {
                            // Player will need to sign-in explicitly using via UI
                            startSignInIntent();
                        }
                    }
                }
            });
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }
    private void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(this,
            new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // at this point, the user is signed out.
                    showGameLogin();
                }
            });
    }

}