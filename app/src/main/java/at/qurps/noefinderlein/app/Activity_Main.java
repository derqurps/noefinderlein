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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.location.LocationServices;
// import com.squareup.leakcanary.LeakCanary;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import at.qurps.noefinderlein.app.basegameutils.BaseGameUtils;


public class Activity_Main extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        Fragment_LocationList.Callbacks,
        Fragment_LocationNear.Callbacks,
        Fragment_LocationFavorits.Callbacks,
        Fragment_Regions.Callbacks,
        Downloader_Destination.Callbacks {

    private static final String TAG = "Activity_Main";
    public static final String KEY_LICENCE_ACCEPTED="licence_accepted_v3";
    public static final String KEY_GAME_SIGN_IN_CLICKED = "game_sign_in_clicked_v2";

    private SharedPreferences mPrefs;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    private SharedPreferences.Editor mEditor;
    public DestinationsDB db;
    public int mActiveyear;
    private boolean mTwoPane;
    public Location mLastLocation;

    private String serverAuthCode;

    private boolean locationUpdatesRunning = false;
    private boolean mDownloadDone = false;
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



    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mGameSignInClicked = false;

    private NavigationView nav_view;
    private FrameLayout mainView;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //JodaTimeAndroid.init(this);
        setContentView(R.layout.activity_list);

        mainView = (FrameLayout) findViewById(R.id.region_list_container);

        nav_view = (NavigationView) findViewById(R.id.nav_view);

        if(nav_view!=null) {
            View headerLayout = nav_view.getHeaderView(0);
            nav_view.setNavigationItemSelectedListener(this);
            yeartext = (TextView) headerLayout.findViewById(R.id.text_noecardyear);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mGameSignInClicked = Util.getPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, false);

        buildGoogleApiClient();

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

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        mEditor.commit();

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

    private void buildGoogleApiClient() {
        buildGoogleApiClient(false);
    }
    private void buildGoogleApiClient(boolean reconnect) {

        // mGameSignInClicked = Util.getPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, false);

        if(mGoogleApiClient == null || reconnect) {
            GoogleApiClient.Builder mGoogleApiClientBuilder = new GoogleApiClient.Builder(this);
            mGoogleApiClientBuilder.addConnectionCallbacks(this);
            mGoogleApiClientBuilder.addOnConnectionFailedListener(this);
            mGoogleApiClientBuilder.addApi(LocationServices.API);
            mGoogleApiClientBuilder.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            mGoogleApiClientBuilder.setViewForPopups(mainView);
            if(mGameSignInClicked) {

                GoogleSignInOptions options = new GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        .build();
                mGoogleApiClientBuilder.addApi(Auth.GOOGLE_SIGN_IN_API, options);
                mGoogleApiClientBuilder.addApiIfAvailable(Games.API).addScope(Games.SCOPE_GAMES);
            }
            mGoogleApiClient = mGoogleApiClientBuilder.build();

        }
    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        initYear();
        notifyDataChanged();
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        if(mDownloadDone && !isGameSignedIn()) {
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
            Log.d(TAG, "Sign-in button clicked");

            mGameSignInClicked = true;

            buildGoogleApiClient(true);
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
            handleSignin();

        } else if (id == R.id.game_logout) {

            mGameSignInClicked = false;
            Util.setPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, mGameSignInClicked);
            setToast(getResources().getString(R.string.game_loggedout),1);
            handleSignOut();

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
        if(overwriteyear){
            year = Integer.valueOf(mPrefs.getString(Activity_Settings.KEY_PREF_OVERWRITE_YEAR_MAN, "2014"));
            Log.d(TAG, String.valueOf(year));
            setYear(year);
            if(!(db.isYearInDatabase(year))){
                setToast(getResources().getString(R.string.error_year_not_found_in_database),1);
                updateDB();
            }
        }else{
            Calendar currentDay = Calendar.getInstance();
            year = currentDay.get(Calendar.YEAR);
            Log.d(TAG, String.valueOf(year));
            Calendar endOfMarch = Calendar.getInstance();
            endOfMarch.set(year, 3, 1);

            if(currentDay.before(endOfMarch)){
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
        notifyDataChanged();
        if(mGoogleApiClient!= null && !mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
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
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed() called, result: " + result);
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
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
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended() called: " + cause);
        if(mDownloadDone) {
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }

    /*@Override
    public void onConnected(Bundle connectionHint) {
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
        Log.d(TAG, "onconnected");

        if(isGameSignedIn()){
            Log.d(TAG, "Connected successful to games");
        }
    }*/

    @Override
    public void onConnected(@Nullable final Bundle connectionHint) {
        if (mGoogleApiClient.hasConnectedApi(Games.API)) {
            Log.d(TAG, "onConnected now");

            Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient).setResultCallback(
                    new ResultCallback<GoogleSignInResult>() {
                        @Override
                        public void onResult(
                                @NonNull GoogleSignInResult googleSignInResult) {
                            if (googleSignInResult.isSuccess()) {
                                onSignedIn(googleSignInResult.getSignInAccount(),
                                        connectionHint);
                            } else {
                                Log.e(TAG, "Error with silentSignIn: " +
                                        googleSignInResult.getStatus());
                                // Don't show a message here, this only happens
                                // when the user can be authenticated, but needs
                                // to accept consent requests.
                                handleSignOut();
                            }
                        }
                    }
            );
        } else {
            handleSignOut();
        }
        isGameSignedIn();
    }


    @Override
    public void onRegionSelected(int i, String name) {
        startDefaultScreen(true, i, name, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult RC_SIGN_IN, responseCode="
                    + responseCode + ", intent=" + intent);
            mGameSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (responseCode == RESULT_OK) {
                mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                if(!isGameSignedIn()) {
                    BaseGameUtils.showActivityResultError(this,
                            requestCode, responseCode, R.string.sign_in_failed);
                }
            }
            isGameSignedIn();
        } else {
            super.onActivityResult(requestCode, responseCode, intent);
        }
    }
    private boolean isGameSignedIn() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() && mGoogleApiClient.hasConnectedApi(Games.API)){
            mGameSignInClicked = true;
            Util.setPreferencesBoolean(this, KEY_GAME_SIGN_IN_CLICKED, mGameSignInClicked);
            showGameLogout();
            return true;
        } else {
            showGameLogin();
            return false;
        }
    }
    public void onShowAchievementsRequested() {
        if (isGameSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), RC_UNUSED);
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.achievements_not_available)).show();
        }
    }

    /**
     * Show a meaningful error when sign-in fails.
     *
     * @param errorCode - The errorCode.
     */
    public void showSignInError(int errorCode) {
        Dialog dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this, errorCode, RC_SIGN_IN);
        if (dialog != null) {
            dialog.show();
        } else {
            // no built-in dialog: show the fallback error message
            (new AlertDialog.Builder(this))
                    .setMessage("Could not sign in!")
                    .setNeutralButton(android.R.string.ok, null)
                    .show();
        }
    }

    public void onShowLeaderboardsRequested() {
        if (isGameSignedIn()) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), RC_UNUSED); // , getString(R.string.mainLeaderboardId)
        } else {
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
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

    /**
     * Handle being signed in successfully.  The account information is
     * populated from the Auth API, and now Games APIs can be called.
     * <p/>
     * Here the server auth code is read from the account.
     * <p>
     * </p>
     *
     * @param acct   - the Google account information.
     * @param bundle - the connection Hint.
     */
    public void onSignedIn(GoogleSignInAccount acct, @Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        serverAuthCode = acct.getServerAuthCode();

    }

    private void handleSignOut() {
        // sign out.
        Log.d(TAG, "Sign-out button clicked");
        if (mGoogleApiClient.hasConnectedApi(Games.API)) {
            Games.signOut(mGoogleApiClient);
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        }
        isGameSignedIn();

    }

    private void handleSignin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        isGameSignedIn();
    }


}