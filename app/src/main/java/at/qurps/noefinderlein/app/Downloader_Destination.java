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
import android.text.TextUtils;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

    private Callbacks mCallbacks;

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
                int daysChangeCount = jsonObj.getInt("daysChangeCount");
                Log.d("Response: ", "> " + String.valueOf(changeid) + " " + String.valueOf(daysChangeId) + " " + String.valueOf(year) );
                int updateneeded = db.updateForYearNeeded(year, changeid);
                int currentChangeIdInDB = db.getCurrentLastChangeId(year);
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

                    String putBody = db.getStringAktDates(year);
                    Log.d(TAG, putBody);

                    jsonStrakt = shput.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Locations/getChangedDestinationIds", ServiceHandler_GETPOSTPUT.PUT, null, putBody);
                    Log.d(TAG, String.valueOf(jsonStrakt));

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
                    int downloadChangeAnz = daysChangeCount;
                    int anzPackages = (downloadChangeAnz / dayPkgCount) + 1;
                    Log.d(TAG, " gesamt und anzahl an x packages " + String.valueOf(downloadChangeAnz) + " " +String.valueOf(anzPackages)  );
                    progress.setMax(downloadChangeAnz);
                    progress.setProgress(Integer.valueOf(0));

                    publishProgress("Updating opening days  ...");
                    downloadSegment(year, daysChangeId, anzPackages, 0, 0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try{
                String jsonStrDel;
                ServiceHandler_GETPOSTPUT shgetdel = new ServiceHandler_GETPOSTPUT();
                jsonStrDel = shgetdel.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Locations/findAllIdsToYear?year="+String.valueOf(year), ServiceHandler_GETPOSTPUT.GET);
                if (jsonStrDel != null) {
                    try {
                        JSONArray nummern = new JSONArray(jsonStrDel);
                        if(nummern.length()>3) {
                            db.delItemsNotInArray(year, nummern);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mCallbacks.onDownloadCompleted();
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

    protected void downloadSegment(int year, int completeEndChangeId, int pkgCount, int progressC, int counter) {
        int beginsegment = db.getCurrentLastChangeId(year);
        if(counter < (pkgCount+20) && beginsegment < completeEndChangeId) {
            ServiceHandler_GETPOSTPUT sh = new ServiceHandler_GETPOSTPUT();
            try {
                String jsonDays = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path) + "Days/getChangeSegmentCount?year=" + String.valueOf(year) + "&changeStart=" + String.valueOf(beginsegment) + "&count=" + String.valueOf(dayPkgCount), ServiceHandler_GETPOSTPUT.GET);
                JSONArray jsonDay = new JSONArray(jsonDays);
                Log.d(TAG, " got anz changes " + String.valueOf(jsonDay.length()) );
                int inserted = updateOrInsertJsondata(jsonDay, year, progressC);
                progress.setProgress(Integer.valueOf((progressC+inserted)));
                Log.d(TAG, String.valueOf(year) + " " +String.valueOf(completeEndChangeId) + " " +String.valueOf(pkgCount) + " " +String.valueOf((progressC+inserted)) + " " + String.valueOf((counter+1)) );
                downloadSegment(year, completeEndChangeId, pkgCount, (progressC+inserted), (counter+1));
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

        try {
            List<String> idList = new ArrayList<String>();

            for(int i=0;i<anzahlakt;i++) {
                idList.add(String.valueOf(nummern.getInt(i)));
            }
            int partitionSize = 20;
            List<List<String>> partitions = new LinkedList<List<String>>();
            for (int i = 0; i < anzahlakt; i += partitionSize) {
                partitions.add(idList.subList(i,
                        Math.min(i + partitionSize, idList.size())));
            }
            for (int i = 0; i < partitions.size(); i++) {
                String putBody = "{\"arr\":[" + TextUtils.join(",", partitions.get(i)) + "]}";
                String jsonLocId = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path) + "Locations/getLocationsToIds" , ServiceHandler_GETPOSTPUT.PUT, null, putBody);
                JSONArray jsonarray = new JSONArray(jsonLocId);

                db.insertOrReplaceLocations(jsonarray);
                zael = zael + jsonarray.length();
                mBuilder.setProgress(anzahlakt, zael, false);
                mNotifyManager.notify(2, mBuilder.build());
                progress.setProgress(Integer.valueOf(zael));
                /*String currentLocationName = "";
                try {
                    currentLocationName = jsonarray.getJSONObject(0).getString(DB_Location_NoeC.KEY_NAME);
                } catch (Exception e) {
                }
                publishProgress("Updating Location Data ...\n" + currentLocationName);*/
            }
        } catch (JSONException e) {
            Log.e("Exception1", String.valueOf(e));
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("LocationSaveException", String.valueOf(e));
            e.printStackTrace();
        }
    }




}
