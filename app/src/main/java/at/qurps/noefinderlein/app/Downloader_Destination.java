package at.qurps.noefinderlein.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by roman on 12.04.14.
 */
public class Downloader_Destination extends AsyncTask<Integer, String, Void> {

    // JSON Node names
    private static final String TAG_JQUERY = "jquery";
    private static final String TAG_DESTINATION = "Destination";
    private static final String TAG_NUMMERN = "nummern";

    public static final String TAG = "Downloader_Destination";



    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    private Context mContext;
    private DestinationsDB db;
    private int progress_status;
    private ProgressDialog progress = null;
    private ProgressDialog.Builder progressBuilder = null;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder = null;
    private int notifyID = 2;
    private int dayPkgCount = 500;
    private static SharedPreferences prefs;

    private Callbacks mCallbacks = sDummyCallbacks;

    public Downloader_Destination(Context context, Activity_Main acti) {
        super();
        this.mContext = context;
        this.db= new DestinationsDB(this.mContext);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        progress = new ProgressDialog(acti);
        if (!(acti instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement callbacks.");
        }
        mCallbacks = (Callbacks) acti;
    }

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onDownloadCompleted();
        public void onDownloadCompleted(int id);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onDownloadCompleted() {
        }
        public void onDownloadCompleted(int id) {
        }
    };

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progress.setCancelable(true);
        progress.setMessage("Updating Location Data ...");
        progress.setTitle("Data Download");
        progress.setIcon(ContextCompat.getDrawable(mContext, R.drawable.ic_cloud_download));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();
        //progress.show(mContext, "Data Download", "Updating Data ...");

        //mNotifyManager =
        //        (NotificationManager) this.mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Showing progress dialog
        /*pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();*/

    }

    @Override
    protected Void doInBackground(Integer...pParams) {
        int year = pParams[0];
        // contacts JSONArray
        JSONObject jquery = null;
        JSONObject destinations= null;
        // Creating service handler class instance
        ServiceHandler_GETPOSTPUT sh = new ServiceHandler_GETPOSTPUT();
        boolean loadOpenData = this.prefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, false);


        String jsonStr = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Changevals/getCurrentIds?year="+String.valueOf(year), ServiceHandler_GETPOSTPUT.GET);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                int changeid = jsonObj.getInt("changeid");
                int daysChangeId = jsonObj.getInt("daysChngId");
                Log.d("Response: ", "> " + String.valueOf(changeid) + " " + String.valueOf(daysChangeId) + " " + String.valueOf(year) );
                int updateneeded = db.updateForYearNeeded(year, changeid);
                int currentChangeIdInDB = db.updateForOpenDaysNeeded(year, daysChangeId);
                Log.d("Update neeeded?: ", String.valueOf(updateneeded));
                if(updateneeded == 1 ){
                    mBuilder = new NotificationCompat.Builder(mContext);
                    mBuilder.setContentTitle("Data Download")
                            .setContentText("Download in progress")
                            .setSmallIcon(R.drawable.noefinderlein_outline_white);

                    Intent resultIntent = new Intent(this.mContext, Activity_Main.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.mContext);
                    stackBuilder.addParentStack(Activity_Main.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    mNotifyManager =
                            (NotificationManager) this.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyManager.notify(2, mBuilder.build());


                    String jsonStrakt;
                    ServiceHandler_GETPOSTPUT shput = new ServiceHandler_GETPOSTPUT();
                    if(year==2016 && !db.areNumbersAvailable(year)){
                        jsonStrakt = shput.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Locations/findAllIdsToYear?year="+String.valueOf(year), ServiceHandler_GETPOSTPUT.GET);
                    }else {
                        String putBody = db.getStringAktDates(year);
                        Log.d(TAG, putBody);

                        jsonStrakt = shput.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Locations/getChangedDestinationIds", ServiceHandler_GETPOSTPUT.PUT, null, putBody);
                        Log.d(TAG, String.valueOf(jsonStrakt));
                    }
                    if (jsonStrakt != null) {
                        try {
                            JSONArray nummern = new JSONArray(jsonStrakt);
                            updatewiththisJsondata(nummern);
                            db.updateChangeId(year,changeid);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("ServiceHandler", "Couldn't get any data from the url");
                    }

                }
                Log.d("Day Update neeeded?: ", String.valueOf(loadOpenData) +" "+ String.valueOf(currentChangeIdInDB) +" "+ String.valueOf(daysChangeId) +" "+ String.valueOf(currentChangeIdInDB < daysChangeId));
                if(loadOpenData && currentChangeIdInDB < daysChangeId) {
                    int downloadChangeAnz = daysChangeId - currentChangeIdInDB;
                    int anzPackages = (downloadChangeAnz / dayPkgCount) + 1;
                    Log.d(TAG, " gesamt und anzahl an x packages " + String.valueOf(downloadChangeAnz) + " " +String.valueOf(anzPackages)  );
                    progress.setMax(downloadChangeAnz);
                    progress.setProgress(Integer.valueOf(0));

                    publishProgress("Updating opening days  ...");
                    downloadSegment(year, currentChangeIdInDB, daysChangeId, anzPackages, 0, 0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        progress.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if(mBuilder!= null ) {
            mBuilder.setContentText("Download complete");
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(2, mBuilder.build());
            mNotifyManager.cancel(2);
        }
        if(progress.isShowing()) {
            progress.dismiss();
        }
        mCallbacks.onDownloadCompleted();
    }

    protected void downloadSegment(int year, int beginsegment, int completeEndChangeId, int pkgCount, int progressC, int counter) {
        if(counter < (pkgCount+20) && beginsegment < completeEndChangeId) {
            ServiceHandler_GETPOSTPUT sh = new ServiceHandler_GETPOSTPUT();
            try {
                int endSegment = (beginsegment + dayPkgCount);
                String jsonDays = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path) + "Days/getChangeSegment?year=" + String.valueOf(year) + "&changeStart=" + String.valueOf(beginsegment) + "&changeEnd=" + String.valueOf(endSegment), ServiceHandler_GETPOSTPUT.GET);
                JSONArray jsonDay = new JSONArray(jsonDays);
                int inserted = updateOrInsertJsondata(jsonDay, year, progressC);
                progress.setProgress(Integer.valueOf((progressC+inserted)));
                Log.d(TAG, String.valueOf(year) + " " + String.valueOf(endSegment) + " " +String.valueOf(completeEndChangeId) + " " +String.valueOf(pkgCount) + " " +String.valueOf((progressC+inserted)) + " " + String.valueOf((counter+1)) );
                downloadSegment(year, endSegment, completeEndChangeId, pkgCount, (progressC+inserted), (counter+1));
            } catch (JSONException e) {
                Log.e("Exception1", String.valueOf(e));
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("DaySaveException", String.valueOf(e));
                e.printStackTrace();
            }
        }
    }
    protected int updateOrInsertJsondata(JSONArray jsonDay, int year, int start) throws Exception {
        String currentDay = "";
        try{
            currentDay = jsonDay.getJSONObject(0).getString("d");
        } catch (Exception e){}
        publishProgress("Updating opening days ...\n"+currentDay);
        db.insertOrReplaceDays(jsonDay, year);
        return jsonDay.length();
    }
    protected void updatewiththisJsondata(JSONArray nummern){
        ServiceHandler_GETPOSTPUT sh = new ServiceHandler_GETPOSTPUT();
        Integer anzahlakt = nummern.length();
        Integer zael = 1;
        progress.setMax(anzahlakt);
        for (int i = 0; i < nummern.length(); i++) {

            try {
                Log.e("Test", String.valueOf(nummern.getInt(i)));

                String jsonLocId = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path) + "Locations/" + String.valueOf(nummern.getInt(i)), ServiceHandler_GETPOSTPUT.GET);
                JSONObject jsonLoc = new JSONObject(jsonLocId);
                Log.d(TAG, jsonLoc.toString());

                mBuilder.setProgress(anzahlakt, zael, false);
                mNotifyManager.notify(2, mBuilder.build());
                progress.setProgress(Integer.valueOf(zael));
                String currentLocationName = "";
                try{
                    currentLocationName = jsonLoc.getString(DB_Location_NoeC.KEY_NAME);
                } catch (Exception e){}
                publishProgress("Updating Location Data ...\n"+currentLocationName);
                updatewiththisJsonobj(jsonLoc);
                zael++;
            }catch (JSONException e) {
                Log.e("Exception1", String.valueOf(e));
                e.printStackTrace();
            }
        }
    }

    protected void updatewiththisJsonobj(JSONObject destination) {
        try {
            int id = destination.getInt(DB_Location_NoeC.KEY_ID);
            boolean updateornew=db.updateornewForItemNeeded(id);
            if(!updateornew){

                    DB_Location_NoeC newloc = new DB_Location_NoeC();
                    newloc.setId(id);
                    newloc.setNummer(destination.getInt(DB_Location_NoeC.KEY_NUMMER));
                    newloc.setJahr(destination.getInt(DB_Location_NoeC.KEY_JAHR));
                    newloc.setKat(destination.getInt(DB_Location_NoeC.KEY_KAT));
                    newloc.setReg(destination.getInt(DB_Location_NoeC.KEY_REG));
                    newloc.setName(destination.getString(DB_Location_NoeC.KEY_NAME));
                    newloc.setLatitude(destination.getDouble(DB_Location_NoeC.KEY_LAT));
                    newloc.setLongitude(destination.getDouble(DB_Location_NoeC.KEY_LON));
                    newloc.setChanged_date("2000.01.01");
                    newloc.setChange_index(-1);
                    db.addMinimalLocation(newloc);

            }
            updatewiththisdata(destination,id);
        } catch (JSONException e) {
            Log.e("Exception2", String.valueOf(e));
            e.printStackTrace();
        }
    }

    protected void updatewiththisdata(JSONObject destinations,int id) {
        try {
            DB_Location_NoeC newloc = db.getLocationToId(id);
            newloc.setId(destinations.getInt(DB_Location_NoeC.KEY_ID));
            newloc.setNummer(destinations.getInt(DB_Location_NoeC.KEY_NUMMER));
            newloc.setJahr(destinations.getInt(DB_Location_NoeC.KEY_JAHR));
            newloc.setKat(destinations.getInt(DB_Location_NoeC.KEY_KAT));
            newloc.setReg(destinations.getInt(DB_Location_NoeC.KEY_REG));
            newloc.setName(destinations.getString(DB_Location_NoeC.KEY_NAME));
            newloc.setEmail(destinations.getString(DB_Location_NoeC.KEY_EMAIL));
            newloc.setLatitude(destinations.getDouble(DB_Location_NoeC.KEY_LAT));
            newloc.setLongitude(destinations.getDouble(DB_Location_NoeC.KEY_LON));
            newloc.setAdr_plz(destinations.getString(DB_Location_NoeC.KEY_ADR_PLZ));
            newloc.setTel(destinations.getString(DB_Location_NoeC.KEY_TEL));
            newloc.setFax(destinations.getString(DB_Location_NoeC.KEY_FAX));
            newloc.setAnreise(destinations.getString(DB_Location_NoeC.KEY_ANREISE));
            newloc.setGeoeffnet(destinations.getString(DB_Location_NoeC.KEY_GEOEFFNET));
            newloc.setAdr_ort(destinations.getString(DB_Location_NoeC.KEY_ADR_ORT));
            newloc.setAdr_street(destinations.getString(DB_Location_NoeC.KEY_ADR_STREET));
            newloc.setTipp(destinations.getString(DB_Location_NoeC.KEY_TIPP));
            newloc.setRollstuhl(destinations.getBoolean(DB_Location_NoeC.KEY_ROLLSTUHL));
            newloc.setKinderwagen(destinations.getBoolean(DB_Location_NoeC.KEY_KINDERWAGEN));
            newloc.setHund(destinations.getBoolean(DB_Location_NoeC.KEY_HUND));
            newloc.setGruppe(destinations.getBoolean(DB_Location_NoeC.KEY_GRUPPE));
            newloc.setWebseite(destinations.getString(DB_Location_NoeC.KEY_WEBSEITE));
            newloc.setBeschreibung(destinations.getString(DB_Location_NoeC.KEY_BESCHREIBUNG));
            newloc.setAussersonder(destinations.getString(DB_Location_NoeC.KEY_AUSSERSONDER));
            newloc.setEintritt(destinations.getString(DB_Location_NoeC.KEY_EINTRITT));
            newloc.setErsparnis(destinations.getString(DB_Location_NoeC.KEY_ERSPARNIS));
            newloc.setTop_ausflugsziel(destinations.getBoolean(DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL));

            newloc.setChanged_date(destinations.getString(DB_Location_NoeC.KEY_CHANGED_DATE));
            newloc.setChange_index(destinations.getInt(DB_Location_NoeC.KEY_CHANGE_INDEX));
            db.updateLocation(newloc);
            mCallbacks.onDownloadCompleted(id);

        } catch (JSONException e) {
            Log.e("Exception3", String.valueOf(e));
            e.printStackTrace();
        }
    }

}
