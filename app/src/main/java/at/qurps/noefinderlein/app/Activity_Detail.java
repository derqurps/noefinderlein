package at.qurps.noefinderlein.app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Activity_Detail extends AppCompatActivity {

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

    private Location_NoeC ziel;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        setContentView(R.layout.activity_detail);
        rootView = getWindow().getDecorView().getRootView();
        Toolbar toolbar = (Toolbar) findViewById(R.id.detailtoolbar);
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
        changeView(getIntent().getExtras());




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
                //db.updateFavorit(ziel);
                return true;
            case R.id.actionb_destination_visited:
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                // Create and show the dialog.
                Bundle argumentsa = new Bundle();
                argumentsa.putInt(DialogFragment_ChooseCheckinDate.ARG_ITEMID, ziel.getId());
                argumentsa.putInt(DialogFragment_ChooseCheckinDate.ARG_YEAR, ziel.getJahr());

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


}
