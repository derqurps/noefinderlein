package at.qurps.noefinderlein.app;

import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.aviadmini.quickimagepick.PickCallback;
import com.aviadmini.quickimagepick.PickSource;
import com.aviadmini.quickimagepick.PickTriggerResult;
import com.aviadmini.quickimagepick.QiPick;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaderFactory;
import com.bumptech.glide.load.model.LazyHeaders;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypertrack.hyperlog.HyperLog;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
//import com.squareup.leakcanary.LeakCanary;



import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class Activity_Detail extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        DialogFragment_ChooseCheckinDate.Callbacks,
        DialogFragment_PictureConsent.NoticeDialogListener,
        ArrayAdapter_Pictures.ClickThumbPictureCallback {

    public static final String ARG_ITEM_ID = "item_id" ;
    public static final String ARG_ITEM_JAHR = "item_jahr" ;
    public static final String ARG_MTWOPANE ="mTwoPane" ;
    public static final String TAG = "Activity_Detail";

    public static final String STORAGE_PATH_UPLOADS = "picuploads/";
    public static final String DATABASE_PATH_UPLOADS = "picuploads";

    public static final String DATABASE_PATH_LIVE = "piclive";

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

    // private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
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
    private static final int PICK_IMAGE = 9156;
    private static final int UPLOAD_NOTIFICATION = 9584;

    AccomplishmentsOutbox mOutbox;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ImageView im;
    private NotificationManager mNotifyManager;

    private ArrayAdapter_Pictures adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private List<CloudPicture> uploadsCP;
    private boolean loadPictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setContentView(R.layout.activity_detail);
        rootView = getWindow().getDecorView().getRootView();
        toolbar = (Toolbar) findViewById(R.id.detailtoolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        loadPictures = prefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_PICTURES, true);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        TextView copyrightText = (TextView) findViewById(R.id.copyright_text);
        im = (ImageView)findViewById(R.id.header_logo);
        if(loadPictures) {
            fabAddStandardPicture();
        } else {
            im.setImageResource(android.R.color.transparent);
            fabAddNavigationPicture();
        }
        //toolbar = (Toolbar) rootView.findViewById(R.id.detailtoolbar);


        uploadsCP = new ArrayList<CloudPicture>();

        setupActionBar();
        this.db = new DestinationsDB(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    HyperLog.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    HyperLog.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mAuth.signInAnonymously()
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                HyperLog.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    HyperLog.w(TAG, "signInAnonymously", task.getException());
                    Toast.makeText(Activity_Detail.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }

                // ...
            }
        });
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(DATABASE_PATH_UPLOADS);
        aktID = getIntent().getIntExtra(ARG_ITEM_ID, 0);
        DB_Location_NoeC location = this.db.getLocationToId(aktID);
        DatabaseReference mDatabaseLiveRef = FirebaseDatabase.getInstance().getReference(DATABASE_PATH_LIVE).child(String.valueOf(location.getNoecIndex()));


        if(loadPictures) {
            mDatabaseLiveRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    List<CloudPicture> uploadsOwn = new ArrayList<>();
                    //iterating through all the values in database
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        CloudPicture upload = postSnapshot.getValue(CloudPicture.class);
                        uploadsOwn.add(upload);
                    }
                    if (uploadsOwn.size() > 0) {
                        // uploadsCP
                        uploadsOwn.addAll(uploadsCP);
                        uploadsCP = uploadsOwn;
                        redrawPictures();
                        // copyrightText.setVisibility(View.VISIBLE);
                        //Glide.with(mContext).load(uploads.get(0).getUrl()).centerCrop().into(im);

                    /*
                    //creating adapter
                    adapter = new ArrayAdapter_Pictures(getApplicationContext(), uploads);

                    //adding adapter to recyclerview
                    recyclerView.setAdapter(adapter);
                    */
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        mGameSignInClicked = Util.getPreferencesBoolean(this, Activity_Main.KEY_GAME_SIGN_IN_CLICKED, false);

        /*if(mGoogleApiClient == null ) {
            GoogleApiClient.Builder mGoogleApiClientBuilder = new GoogleApiClient.Builder(this);
            mGoogleApiClientBuilder.addConnectionCallbacks(this);
            mGoogleApiClientBuilder.addApi(LocationServices.API);
            mGoogleApiClientBuilder.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            mGoogleApiClientBuilder.setViewForPopups(rootView);
            mGoogleApiClient = mGoogleApiClientBuilder.build();

        }*/
        changeView(getIntent().getExtras());
        aktjahr = getIntent().getIntExtra(ARG_ITEM_JAHR, 0);
        mOutbox = new AccomplishmentsOutbox(this, aktjahr, this.db);
        mOutbox.loadLocal();


        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(getApplication());*/
    }

    private void fabAddStandardPicture(){
        fab.setImageResource(R.drawable.ic_add_a_photo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start image picker
                startPicker();

            }
        });
    }
    private void fabAddNavigationPicture(){
        fab.setImageResource(R.drawable.ic_navigation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start image picker
                startNavigate();

            }
        });
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
        if (mGameSignInClicked) {
            signInSilently();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                //NavUtils.navigateUpFromSameTask(this);
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
                //HyperLog.d(TAG,String.valueOf(mMenu.findItem(R.id.actionb_favorit_star).getIcon()));
                //HyperLog.d(TAG,String.valueOf(getResources().getDrawable(R.drawable.ic_action_star_0)));
                Drawable drawable;
                if(ziel.getFavorit())
                {
                    drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_star_outline);
                    ziel.setFavorit(false);
                    HyperLog.d(TAG +"false",String.valueOf(ziel.getFavorit()));
                }
                else
                {
                    drawable = ContextCompat.getDrawable(mContext,R.drawable.ic_star);
                    ziel.setFavorit(true);
                    HyperLog.d(TAG +"true",String.valueOf(ziel.getFavorit()));

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




                //HyperLog.d(TAG,String.valueOf(mMenu.findItem(R.id.actionb_favorit_star).getIcon()));
                //HyperLog.d(TAG,String.valueOf(getResources().getDrawable(R.drawable.ic_action_star_0)));

                /*if(ziel.getAngesehen())
                {
                    mMenu.findItem(R.id.actionb_destination_visited).setIcon(R.mipmap.ic_action_tick);
                    ziel.setAngesehen(false);
                    //HyperLog.d(TAG +"false",String.valueOf(ziel.getAngesehen()));
                }
                else
                {
                    mMenu.findItem(R.id.actionb_destination_visited).setIcon(R.mipmap.ic_action_tick_grey);
                    ziel.setAngesehen(true);
                    //HyperLog.d(TAG +"true",String.valueOf(ziel.getAngesehen()));

                }
                //db.updateAngesehen(ziel);*/
                return true;
            case R.id.actionb_upload_picture:
                startPicker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean updateOptionsMenu(){
        if(ziel != null) {
            HyperLog.d(TAG , String.valueOf(ziel.getFavorit()));
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
    private void showOnMap() {
        String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f(%s)", ziel.getLatitude(), ziel.getLongitude(), ziel.getName().replace("&","-"));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void startPicker() {

        @PickTriggerResult final int triggerResult;
        triggerResult = QiPick.in(this)
                .allowOnlyLocalContent(true)
                .fromMultipleSources("All sources", PickSource.CAMERA, PickSource.GALLERY);
        this.solveTriggerResult(triggerResult);
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

    private void redrawPictures() {
        RecyclerView rvMoving = (RecyclerView) findViewById(R.id.header_logo_gall);
        rvMoving.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        adapter = new ArrayAdapter_Pictures(getApplicationContext(), uploadsCP, findViewById(R.id.toolbar_layout).getHeight());
        adapter.setCallback(this);
        rvMoving.setAdapter(adapter);
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


            if(ziel.getGooglePlaceId()!=null && loadPictures){

                //"https://maps.googleapis.com/maps/api/place/details/json?placeid=" + ziel.getGooglePlaceId() + "&key=" + getString(R.string.google_photo_key)
                String loadUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + ziel.getGooglePlaceId() + "&key=" + getString(R.string.google_photo_key);
                HyperLog.d(TAG, loadUrl);
                Ion.with(mContext)
                    .load(loadUrl )
                    .setHeader("Referer", "https://noecard.reitschmied.at")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                            try {

                                JsonObject newresult = result.getAsJsonObject("result");
                                JsonArray photos = newresult.getAsJsonArray("photos");
                                for (int i = 0; i < photos.size(); i++) {
                                    JsonObject photo = photos.get(i).getAsJsonObject();
                                    String photo_reference = photo.get("photo_reference").getAsString();

                                    uploadsCP.add(new CloudPicture(photo_reference, ziel.getName()));
                                }


                                /*GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                                    .addHeader("Referer", "https://noecard.reitschmied.at")
                                    .build());
                                Glide.with(mContext).load(glideUrl).centerCrop().into(im);*/
                                redrawPictures();
                                // toolbar.setBackgroundResource(R.drawable.detailmenubackground);
                            }catch(Exception exce){}
                        }
                    });

            } else {
                // toolbar.setBackgroundResource(android.R.color.transparent);
            }
            TextView name=((TextView) rootView.findViewById(R.id.detail_text_title));
            name.setText(String.valueOf(ziel.getName().toString() ));

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
                        showOnMap();
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
                ((RelativeLayout) rootView.findViewById(R.id.detail_navi_rellay)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startNavigate();
                    }
                });
            } else {
                ((RelativeLayout) rootView.findViewById(R.id.detail_navi_rellay)).setVisibility(View.GONE);
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
        HyperLog.d("da","da");
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
                HyperLog.d("nr + jahr: ", String.valueOf(arguments.getInt(ARG_ITEM_ID))+" " + String.valueOf(arguments.getInt(ARG_ITEM_JAHR)));
                aktID = arguments.getInt(ARG_ITEM_ID);
                aktjahr = arguments.getInt(ARG_ITEM_JAHR);
                if(db != null) {
                    ziel = db.getLocationToId(aktID);
                    HyperLog.d(TAG, String.valueOf(ziel.getFavorit()));
                }

            }
            if (arguments.containsKey(ARG_MTWOPANE)) {
                mTwoPane=arguments.getBoolean(ARG_MTWOPANE);
            }
        }
    }
    private boolean isGameSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }
    @Override
    protected void onStart() {
        HyperLog.d(TAG, "onStart()");
        super.onStart();

        /*if(!isGameSignedIn()) {
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }*/
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    protected void onStop() {
        HyperLog.d(TAG, "onStop()");
        super.onStop();
        /*if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }*/
    }

    @Override
    public void onConnected(@Nullable final Bundle connectionHint) {
        /*if (mGoogleApiClient.hasConnectedApi(LocationServices.API)) {
            getLastKnownLocation();

        }*/

        isGameSignedIn();
    }


    @Override
    public void onConnectionSuspended(int i) {
        HyperLog.d(TAG, "onConnectionSuspended() called: " );

        //mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);

    }

    @Override
    public void onItemSelected_DialogFragment_ChooseCheckinDate(int id) {
        HyperLog.d(TAG, "onItemSelected_DialogFragment_ChooseCheckinDate() called: " );
        checkAchievements(id);
    }

    private void checkAchievements(int id) {
        HyperLog.d(TAG, "checkAchievements() called: " );
        Util.setToast(this, "check achievements", 0);
        mOutbox.checkForAchievements(id);

        mOutbox.updateLeaderboards(id);

        mOutbox.pushAccomplishments(id);
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
        if(ActivityCompat.checkSelfPermission(Activity_Detail.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Activity_Detail.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Activity_Detail.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, LAST_LOCATION_REQUEST);
            return;
        }
        mFusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mLastLocation = location;
                        mLatitude = mLastLocation.getLatitude();
                        mLongitude = mLastLocation.getLongitude();
                    }
                }
            });
    }

    @Override
    protected void onActivityResult(final int pRequestCode, final int pResultCode, final Intent pData) {
        super.onActivityResult(pRequestCode, pResultCode, pData);
        QiPick.handleActivityResult(getApplicationContext(), pRequestCode, pResultCode, pData, this.mCallback);
    }

    private final PickCallback mCallback = new PickCallback() {

        @Override
        public void onImagePicked(@NonNull final PickSource pPickSource, final int pRequestType, @NonNull final Uri pImageUri) {
            // Do something with Uri, for example load image into an ImageView
            displayConsentDialog(pImageUri);
        }

        @Override
        public void onMultipleImagesPicked(final int pRequestType, @NonNull final List<Uri> pImageUris) {
            // meh whatever, just show first picked ;D
            for (int i=0; i<pImageUris.size(); i++) {
                this.onImagePicked(PickSource.DOCUMENTS, pRequestType, pImageUris.get(i));
            }

        }

        @Override
        public void onError(@NonNull final PickSource pPickSource, final int pRequestType, @NonNull final String pErrorString) {
            HyperLog.e(TAG, "Err: " + pErrorString);
        }

        @Override
        public void onCancel(@NonNull final PickSource pPickSource, final int pRequestType) {
            HyperLog.d(TAG, "Cancel: " + pPickSource.name());
        }

    };

    private void solveTriggerResult(final @PickTriggerResult int pTriggerResult) {

    }

    private void displayConsentDialog(Uri pImageUri) {

        DialogFragment_PictureConsent dialog = new DialogFragment_PictureConsent();
        dialog.show(getSupportFragmentManager(), "DialogFragment_PictureConsent");

        Bundle args = new Bundle();
        args.putString("imageUri", pImageUri.toString());
        dialog.setArguments(args);

    }

    @Override
    public void onDialogPositiveClick(Uri pImageUri) {
        final String uuid = UUID.randomUUID().toString();
        HyperLog.d(TAG, "uuid: " + uuid);
        String bucketPath = STORAGE_PATH_UPLOADS + uuid + ".png";
        StorageReference fireUpRef = mStorageRef.child(bucketPath);

        String locationId = String.valueOf(ziel.getId());
        String locationName = String.valueOf(ziel.getName());
        String locationnoecIndex = String.valueOf(ziel.getNoecIndex());

        StorageMetadata meta = new StorageMetadata.Builder()
                .setCustomMetadata("id", locationId)
                .setCustomMetadata("num", String.valueOf(ziel.getNummer()))
                .setCustomMetadata("name", String.valueOf(locationName))
                .setCustomMetadata("noec_idx", String.valueOf(locationnoecIndex))
                .build();

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Picture Upload")
                .setContentText("Upload in progress")
                .setSmallIcon(R.drawable.noefinderlein_outline_white);

        UploadTask uploadTask = fireUpRef.putFile(pImageUri, meta);
        Util.setToast(mContext, getString(R.string.imgupconsentthank),Toast.LENGTH_LONG);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests") double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                mBuilder.setProgress(100, (int)progress, false);

                // Displays the progress bar for the first time.
                mNotifyManager.notify(UPLOAD_NOTIFICATION, mBuilder.build());
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mBuilder.setContentText("Download complete").setProgress(0,0,false);
                mNotifyManager.notify(UPLOAD_NOTIFICATION, mBuilder.build());
                mNotifyManager.cancel(UPLOAD_NOTIFICATION);
                System.out.println("Upload is complete");
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                @SuppressWarnings("VisibleForTests") String locationnoecIndex = taskSnapshot.getMetadata().getCustomMetadata("noec_idx");
                @SuppressWarnings("VisibleForTests") String locationName = taskSnapshot.getMetadata().getCustomMetadata("name");
                @SuppressWarnings("VisibleForTests") String locationId = taskSnapshot.getMetadata().getCustomMetadata("id");
                // mDatabaseRef.setValue(downloadUrl.toString());

                String key = mDatabase.getReference().child(DATABASE_PATH_UPLOADS).push().getKey();

                CloudPicture upload = new CloudPicture(locationId, downloadUrl.toString(), locationnoecIndex, locationName);
                Map<String, Object> postValues = upload.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + DATABASE_PATH_UPLOADS + "/" + key, postValues);
                mDatabase.getReference().updateChildren(childUpdates);
                Util.setToast(mContext, getString(R.string.imgupcomplete) ,Toast.LENGTH_LONG);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                System.out.println("Upload is failed");
            }
        });
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
                        GamesClient gamesClient = Games.getGamesClient(Activity_Detail.this, signedInAccount);
                        gamesClient.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                        gamesClient.setViewForPopups(rootView);
                    } else {
                        // Player will need to sign-in explicitly using via UI
                    }
                }
            });
    }


    @Override
    public void thumbPictureClicked(int position) {

        ArrayList<CloudPicture> upList = adapter.getSortedList(position);

        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("upList", upList);
        arguments.putInt("position", position);

        Intent intent = new Intent(mContext, Activity_PictureSlider.class);
        intent.putExtras(arguments);
        mContext.startActivity(intent);
    }
}
