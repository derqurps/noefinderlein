package at.qurps.noefinderlein.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import at.qurps.noefinderlein.app.basegameutils.BaseGameUtils;

public class Activity_Detail extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
DialogFragment_ChooseCheckinDate.Callbacks{

    public static final String ARG_ITEM_ID = "item_id" ;
    public static final String ARG_ITEM_JAHR = "item_jahr" ;
    public static final String ARG_MTWOPANE ="mTwoPane" ;
    public static final String TAG = "Activity_Detail";

    static final int REQUEST_IMAGE_CAPTURE = 101;
    private Context mContext;

    public int mYear;
    public int mLocId;
    public DestinationsDB db;
    public Location mCurrentLocation;

    private DB_Location_NoeC ziel;
    private boolean mTwoPane=false;
    private boolean isRegion=false;
    private Menu mMenu;
    private View rootView;
    public ViewGroup mcontainer;
    private TextView beschreibung;
    private TextView adresse;
    private ActionMode mActionMode;
    private int aktjahr;
    private int aktID;
    Util.dataholder dataToSend = new Util.dataholder();
    private SharedPreferences prefs;
    private String fDate;

    private GoogleApiClient mGoogleApiClient;
    private boolean mGameSignInClicked = false;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private Location mLastLocation;
    private double mLatitude = 0;
    private double mLongitude = 0;
    private static final int LAST_LOCATION_REQUEST = 2;

    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    AccomplishmentsOutbox mOutbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        setContentView(R.layout.activity_detail);
        rootView = getWindow().getDecorView().getRootView();
        Toolbar toolbar = (Toolbar) findViewById(R.id.detailtoolbar);
        /*ImageView im = (ImageView)findViewById(R.id.header_logo);
        im.setImageResource(R.drawable.example);*/
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                startNavigate();
            }
        });
        setupActionBar();
        this.db = new DestinationsDB(this);

        mGameSignInClicked = Util.getPreferencesBoolean(this, Activity_Main.KEY_GAME_SIGN_IN_CLICKED, false);

        if(mGoogleApiClient == null ) {
            GoogleApiClient.Builder mGoogleApiClientBuilder = new GoogleApiClient.Builder(this);
            mGoogleApiClientBuilder.addConnectionCallbacks(this);
            mGoogleApiClientBuilder.addOnConnectionFailedListener(this);
            mGoogleApiClientBuilder.addApi(LocationServices.API);
            mGoogleApiClientBuilder.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            mGoogleApiClientBuilder.setViewForPopups(rootView);
            if(mGameSignInClicked) {

                GoogleSignInOptions options = new GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        .build();
                mGoogleApiClientBuilder.addApi(Auth.GOOGLE_SIGN_IN_API, options);
                mGoogleApiClientBuilder.addApiIfAvailable(Games.API).addScope(Games.SCOPE_GAMES);
            }
            mGoogleApiClient = mGoogleApiClientBuilder.build();

        }
        changeView(getIntent().getExtras());
        aktjahr = getIntent().getIntExtra(ARG_ITEM_JAHR, 0);
        mOutbox = new AccomplishmentsOutbox(this, aktjahr, this.db);
        mOutbox.loadLocal();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        Util.colorMenuItems(mContext, menu,R.id.actionb_destination_visited, R.color.noecard_white);
        Util.colorMenuItems(mContext, menu,R.id.actionb_favorit_star, R.color.noecard_white);
        Util.colorMenuItems(mContext, menu,R.id.actionb_navigate_to_dest, R.color.noecard_white);
        Util.colorMenuItems(mContext, menu,R.id.actionb_show_in_map, R.color.noecard_white);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mMenu=menu;
        return updateOptionsMenu();
    }

    @Override
    public void onPause() {
        db.updateLocation(ziel);
        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.actionb_navigate_to_dest:

                startNavigate();

                return true;
            case R.id.actionb_show_in_map:
                Bundle arguments = new Bundle();
                arguments.putInt(Activity_Map.ARG_ITEM_ID, ziel.getId());
                arguments.putInt(Activity_Map.ARG_ITEM_YEAR,ziel.getJahr());
                arguments.putBoolean(Activity_Map.ARG_MTWOPANE, mTwoPane);

                Intent intent = new Intent(mContext, Activity_Map.class);
                intent.putExtras(arguments);
                startActivity(intent);
                return true;

            case R.id.actionb_favorit_star:
                //Log.d(TAG,String.valueOf(mMenu.findItem(R.id.actionb_favorit_star).getIcon()));
                //Log.d(TAG,String.valueOf(getResources().getDrawable(R.drawable.ic_action_star_0)));
                Drawable drawable;
                if(ziel.getFavorit())
                {
                    drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_star_outline);
                    ziel.setFavorit(false);
                    Log.d(TAG +"false",String.valueOf(ziel.getFavorit()));
                }
                else
                {
                    drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_star);
                    ziel.setFavorit(true);
                    Log.d(TAG +"true",String.valueOf(ziel.getFavorit()));

                }
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(mContext, R.color.noecard_white));
                mMenu.findItem(R.id.actionb_favorit_star).setIcon(drawable);
                db.updateFavorit(ziel);
                return true;
            case R.id.actionb_destination_visited:
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                // Create and show the dialog.
                Bundle argumentsa = new Bundle();
                argumentsa.putInt(DialogFragment_ChooseCheckinDate.ARG_ITEMID, ziel.getId());
                argumentsa.putInt(DialogFragment_ChooseCheckinDate.ARG_YEAR, ziel.getJahr());
                argumentsa.putDouble(DialogFragment_ChooseCheckinDate.ARG_LAT, mLatitude);
                argumentsa.putDouble(DialogFragment_ChooseCheckinDate.ARG_LON, mLongitude);

                DialogFragment_ChooseCheckinDate newFragment = new DialogFragment_ChooseCheckinDate();
                newFragment.setArguments(argumentsa);
                newFragment.show(ft, "dialog");




                //Log.d(TAG,String.valueOf(mMenu.findItem(R.id.actionb_favorit_star).getIcon()));
                //Log.d(TAG,String.valueOf(getResources().getDrawable(R.drawable.ic_action_star_0)));

                /*if(ziel.getAngesehen())
                {
                    mMenu.findItem(R.id.actionb_destination_visited).setIcon(R.mipmap.ic_action_tick);
                    ziel.setAngesehen(false);
                    //Log.d(TAG +"false",String.valueOf(ziel.getAngesehen()));
                }
                else
                {
                    mMenu.findItem(R.id.actionb_destination_visited).setIcon(R.mipmap.ic_action_tick_grey);
                    ziel.setAngesehen(true);
                    //Log.d(TAG +"true",String.valueOf(ziel.getAngesehen()));

                }
                //db.updateAngesehen(ziel);*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean updateOptionsMenu(){
        if(ziel != null) {
            Log.d(TAG , String.valueOf(ziel.getFavorit()));
            Drawable drawable;
            if (mMenu != null && ziel.getFavorit()) {
                drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_star);
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(mContext, R.color.noecard_white));
                mMenu.findItem(R.id.actionb_favorit_star).setIcon(drawable);
            } else if(mMenu != null){
                drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_star_outline);
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(mContext, R.color.noecard_white));
                mMenu.findItem(R.id.actionb_favorit_star).setIcon(drawable);
            }

        }
        return true;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private boolean checkifnull(String teststring){
        if (teststring==null || (teststring.equals("")) || (teststring.equals("NULL")) || (teststring.equals("null")) ){
            return false;
        }
        else {
            return true;
        }
    }
    private void startNavigate()
    {

        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?dirflg=d&daddr=%f,%f", ziel.getLatitude(), ziel.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        PackageManager manager = mContext.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
        if (list != null && list.size() > 0) {
            //You have at least one activity to handle the intent
            mContext.startActivity(intent);
        } else {
            //No activity to handle the intent.
            Toast.makeText(mContext, R.string.toast_no_geo_app_found, Toast.LENGTH_SHORT).show();
        }

    }
    public DestinationsDB getDb(){
        return this.db;
    }
    public Location getLocation() {
        if(mCurrentLocation==null)
        {
            mCurrentLocation.setLatitude(0);
            mCurrentLocation.setLongitude(0);
        }
        return mCurrentLocation;
    }

    private void updateView(){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Date cDate = new Date();
        this.fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

        beschreibung=((TextView) rootView.findViewById(R.id.detail_text_beschreibung));
        String ausgabeuntertitel="";
        // Show the dummy content as text in a TextView.
        Spannable sb ;
        String finalString;
        String ersterteil;
        String zweiterteil;
        if (ziel != null) {
            //TODO
            /*((TextView) rootView.findViewById(R.id.detail_text_nummer))
                    .setText(String.valueOf(ziel.getNummer()));
            ((TextView) rootView.findViewById(R.id.detail_text_name))
                    .setText(String.valueOf(ziel.getName()));*/

            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.detailtoolbar);
            //toolbar.setTitle(ziel.getBeschreibung().toString());
            String title = "";
            if(ziel.getNummer()!=0){
                title = String.valueOf(ziel.getNummer()) + "  ";
            }
            title = title + ziel.getName().toString();
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(title);

            RelativeLayout notOpenWarning=((RelativeLayout) rootView.findViewById(R.id.detail_notopenToday));
            TextView notOpenWarningText=((TextView) rootView.findViewById(R.id.detail_not_open_warning_text));
            boolean useOpenData = prefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, false);
            int locationId = ziel.getId();
            if(useOpenData) {

                if(this.db.isOpenToday(locationId)) {
                    notOpenWarning.setVisibility(View.GONE);
                } else {

                    if(Util.isTodaySet(this)) {
                        notOpenWarningText.setText(getResources().getString(R.string.not_open_today));
                    } else {
                        notOpenWarningText.setText(getResources().getString(R.string.not_open_on) + " " + Util.getDisplayDateString(this));
                    }

                    notOpenWarning.setVisibility(View.VISIBLE);
                }
            }

            TextView name=((TextView) rootView.findViewById(R.id.detail_text_title));
            name.setText(String.valueOf(ziel.getName().toString()));

            beschreibung.setText(String.valueOf(Html.fromHtml(ziel.getBeschreibung().toString())));

            if (!ziel.getTop_ausflugsziel())
            {
                ((ImageView) rootView.findViewById(R.id.detail_top_ausflugsziel)).setVisibility(View.INVISIBLE);
            }
            if (!ziel.getGruppe())
            {
                ((ImageView) rootView.findViewById(R.id.detail_gruppe)).setVisibility(View.GONE);
            }
            if (!ziel.getHund())
            {
                ((ImageView) rootView.findViewById(R.id.detail_hund)).setVisibility(View.GONE);
            }
            if (!ziel.getKinderwagen())
            {
                ((ImageView) rootView.findViewById(R.id.detail_kinderwagen)).setVisibility(View.GONE);
            }
            if (!ziel.getRollstuhl())
            {
                ((ImageView) rootView.findViewById(R.id.detail_rollstuhl)).setVisibility(View.GONE);
            }

            adresse=((TextView) rootView.findViewById(R.id.detail_adresscompl));
            String adresstr = "";
            adresstr += String.valueOf(ziel.getAdr_plz());
            adresstr += ", "+String.valueOf(ziel.getAdr_ort());
            if (checkifnull(ziel.getAdr_street())){
                adresstr += ", "+String.valueOf(ziel.getAdr_street());
            }
            adresse.setText(adresstr);


            if (checkifnull(ziel.getTel()))
            {

                ((TextView) rootView.findViewById(R.id.weitere_tel)).setText(String.valueOf(ziel.getTel()));
                ((RelativeLayout) rootView.findViewById(R.id.detail_tel_rellay)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + String.valueOf(ziel.getTel())));
                        startActivity(intent);
                    }
                });
                ((RelativeLayout) rootView.findViewById(R.id.detail_tel_rl)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        v.setSelected(true);
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + String.valueOf(ziel.getTel())));
                        startActivity(intent);
                    }
                });

            }
            else
            {
                ((RelativeLayout) rootView.findViewById(R.id.detail_tel_rellay)).setVisibility(View.GONE);
                ((RelativeLayout) rootView.findViewById(R.id.detail_tel_rl)).setVisibility(View.GONE);


            }

            if (checkifnull(ziel.getEmail()))
            {
                ((TextView) rootView.findViewById(R.id.weitere_mail)).setText(String.valueOf(ziel.getEmail()));
                ((RelativeLayout) rootView.findViewById(R.id.detail_email_rellay)).setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                        /* Fill it with Data */
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{String.valueOf(ziel.getEmail())});

                        /* Send it off to the Activity-Chooser */
                        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    }
                });
                ((RelativeLayout) rootView.findViewById(R.id.detail_mail_rl)).setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                        /* Fill it with Data */
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{String.valueOf(ziel.getEmail())});
                        v.setSelected(true);
                        /* Send it off to the Activity-Chooser */
                        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    }
                });

            }
            else
            {
                ((RelativeLayout) rootView.findViewById(R.id.detail_email_rellay)).setVisibility(View.GONE);
                ((RelativeLayout) rootView.findViewById(R.id.detail_mail_rl)).setVisibility(View.GONE);
            }

            ((RelativeLayout) rootView.findViewById(R.id.detail_weitere_rl)).setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    ((RelativeLayout) rootView.findViewById(R.id.detail_weitere_rl)).setVisibility(View.GONE);
                    ((RelativeLayout) rootView.findViewById(R.id.detail_weitere_items)).setVisibility(View.VISIBLE);
                }
            });

            if (checkifnull(ziel.getWebseite()))
            {
                ((TextView) rootView.findViewById(R.id.weitere_website)).setText(String.valueOf(ziel.getWebseite()));
                ((RelativeLayout) rootView.findViewById(R.id.detail_webseite_rellay)).setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        String url = String.valueOf(ziel.getWebseite());
                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    }
                });
                ((RelativeLayout) rootView.findViewById(R.id.detail_website_rl)).setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        v.setSelected(true);
                        String url = String.valueOf(ziel.getWebseite());
                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    }
                });

            }
            else
            {
                ((RelativeLayout) rootView.findViewById(R.id.detail_webseite_rellay)).setVisibility(View.GONE);
                ((RelativeLayout) rootView.findViewById(R.id.detail_website_rl)).setVisibility(View.GONE);

            }

            ((TextView) rootView.findViewById(R.id.detail_geoeffnet)).setText(String.valueOf(Html.fromHtml(ziel.getGeoeffnet().toString())));

            if (checkifnull(ziel.getAnreise()))
            {
                ((TextView) rootView.findViewById(R.id.detail_anreise)).setText(String.valueOf(Html.fromHtml(ziel.getAnreise().toString())));
            }
            else
            {
                ((RelativeLayout) rootView.findViewById(R.id.detail_anreise_rl)).setVisibility(View.GONE);
            }
            if (ziel.getLatitude()!=0 && ziel.getLongitude()!=0)
            {
                final Location loc = new Location("detail_destination");
                loc.setLatitude(ziel.getLatitude());
                loc.setLongitude(ziel.getLongitude());

                ((TextView) rootView.findViewById(R.id.detail_coordinates_latitude)).setText(String.valueOf(Location.convert(loc.getLatitude(),Location.FORMAT_DEGREES)));

                ((TextView) rootView.findViewById(R.id.detail_coordinates_longitude)).setText(String.valueOf(Location.convert(loc.getLongitude(),Location.FORMAT_DEGREES)));

                ((ImageView) rootView.findViewById(R.id.detail_navigatetopicture)).setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        startNavigate();
                    }
                });
                ((RelativeLayout) rootView.findViewById(R.id.detail_location)).setOnClickListener(new View.OnClickListener(){
                    Integer format=0;
                    public void onClick(View v) {
                        String zwischlat="";
                        String zwischlon="";
                        if(format==0){
                            zwischlat=String.valueOf(Location.convert(loc.getLatitude(),Location.FORMAT_MINUTES));
                            zwischlon=String.valueOf(Location.convert(loc.getLongitude(),Location.FORMAT_MINUTES));
                            String[] zwischlat_teil = zwischlat.split(":");
                            String[] zwischlon_teil = zwischlon.split(":");
                            zwischlat = "N "+ zwischlat_teil[0] + "° "+zwischlat_teil[1];
                            zwischlon = "E "+ zwischlon_teil[0] + "° "+zwischlon_teil[1];
                        }
                        else if(format==1){
                            zwischlat=String.valueOf(Location.convert(loc.getLatitude(),Location.FORMAT_SECONDS));
                            zwischlon=String.valueOf(Location.convert(loc.getLongitude(),Location.FORMAT_SECONDS));
                            String[] zwischlat_teil = zwischlat.split(":");
                            String[] zwischlon_teil = zwischlon.split(":");
                            zwischlat = "N "+ zwischlat_teil[0] + "° "+zwischlat_teil[1]+"' "+zwischlat_teil[2]+"\"";
                            zwischlon = "E "+ zwischlon_teil[0] + "° "+zwischlon_teil[1]+"' "+zwischlon_teil[2]+"\"";
                        }
                        else {
                            zwischlat=String.valueOf(Location.convert(loc.getLatitude(),Location.FORMAT_DEGREES));
                            zwischlon=String.valueOf(Location.convert(loc.getLongitude(),Location.FORMAT_DEGREES));
                            format=-1;
                        }
                        ((TextView) rootView.findViewById(R.id.detail_coordinates_latitude)).setText(zwischlat);
                        ((TextView) rootView.findViewById(R.id.detail_coordinates_longitude)).setText(zwischlon);
                        format++;
                    }
                });
            }
            if (checkifnull(ziel.getTipp()))
            {
                ((TextView) rootView.findViewById(R.id.detail_tipp)).setText(String.valueOf(Html.fromHtml(ziel.getTipp().toString())));
            }
            else
            {
                ((RelativeLayout) rootView.findViewById(R.id.detail_tipp_rl)).setVisibility(View.GONE);
            }
            if (checkifnull(ziel.getAussersonder()))
            {
                ((TextView) rootView.findViewById(R.id.detail_aussersonder)).setText("* außer Sonderveranstaltungen: "+String.valueOf(ziel.getAussersonder()).replace("* außer Sonderveranstaltungen:",""));
            }
            else
            {
                ((RelativeLayout) rootView.findViewById(R.id.detail_aussersonder_rl)).setVisibility(View.GONE);
            }

            ((TextView) rootView.findViewById(R.id.detail_mitdercard))
                    .setText(String.valueOf(ziel.getEintritt()));
            ((TextView) rootView.findViewById(R.id.detail_gespart))
                    .setText(String.valueOf(ziel.getErsparnis()));


            ((TextView) rootView.findViewById(R.id.detail_gespart))
                    .setPaintFlags(((TextView) rootView.findViewById(R.id.detail_gespart)).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );


        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public void changeView(Bundle arguments){
        getNewLocationArguments(arguments);
        Log.d("da","da");
        updateView();
        updateOptionsMenu();
    }
    private void getNewLocationArguments(Bundle arguments){
        if (arguments!=null) {
            if (arguments.containsKey(ARG_ITEM_ID) && arguments.containsKey(ARG_ITEM_JAHR)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
                //		ARG_ITEM_NUMMER));
                Log.d("nr + jahr: ", String.valueOf(arguments.getInt(ARG_ITEM_ID))+" " + String.valueOf(arguments.getInt(ARG_ITEM_JAHR)));
                aktID = arguments.getInt(ARG_ITEM_ID);
                aktjahr = arguments.getInt(ARG_ITEM_JAHR);
                if(db != null) {
                    ziel = db.getLocationToId(aktID);
                    Log.d(TAG, String.valueOf(ziel.getFavorit()));
                }

            }
            if (arguments.containsKey(ARG_MTWOPANE)) {
                mTwoPane=arguments.getBoolean(ARG_MTWOPANE);
            }
        }
    }
    private boolean isGameSignedIn() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() && mGoogleApiClient.hasConnectedApi(Games.API)){
            return true;
        } else {
            return false;
        }
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        if(!isGameSignedIn()) {
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

    @Override
    public void onConnected(@Nullable final Bundle connectionHint) {
        if (mGoogleApiClient.hasConnectedApi(LocationServices.API)) {
            getLastKnownLocation();

        }
        if (mGoogleApiClient.hasConnectedApi(Games.API)) {
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
            if (!mOutbox.isEmpty()) {
                mOutbox.pushAccomplishments(mGoogleApiClient, 0);
                Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            handleSignOut();
        }
        isGameSignedIn();
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

    public void onSignedIn(GoogleSignInAccount acct, @Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        //serverAuthCode = acct.getServerAuthCode();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called: " );

        mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);
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
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
        isGameSignedIn();
    }

    @Override
    public void onItemSelected_DialogFragment_ChooseCheckinDate(int id) {
        checkAchievements(id);
    }

    private void checkAchievements(int id) {

        mOutbox.checkForAchievements(mGoogleApiClient, id);

        mOutbox.updateLeaderboards(mGoogleApiClient, id);

        mOutbox.pushAccomplishments(mGoogleApiClient, id);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
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

    protected void getLastKnownLocation(){
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            if(ActivityCompat.checkSelfPermission(Activity_Detail.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(Activity_Detail.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(Activity_Detail.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, LAST_LOCATION_REQUEST);
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();
            }
        }
    }
}
