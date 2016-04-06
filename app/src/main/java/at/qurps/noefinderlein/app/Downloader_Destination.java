package at.qurps.noefinderlein.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by roman on 12.04.14.
 */
public class Downloader_Destination extends AsyncTask<Integer, Integer, Void> {

    // URL to get contacts JSON
    private static String url = "https://noefinderlein.qurps.at/apidestinations/.json";

    // JSON Node names
    private static final String TAG_JQUERY = "jquery";
    private static final String TAG_DESTINATION = "Destination";
    private static final String TAG_NUMMERN = "nummern";

    public static final String TAG = "Downloader_Destination";



    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    private final Context mContext;
    private DestinationsDB db;
    private int progress_status;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int notifyID = 2;

    public Downloader_Destination(Context context) {
        super();
        this.mContext = context;
        this.db= new DestinationsDB(this.mContext);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //mNotifyManager =
        //        (NotificationManager) this.mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentTitle("Data Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.mipmap.ic_launcher);
        Intent resultIntent = new Intent(this.mContext, Activity_Settings.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.mContext);
        stackBuilder.addParentStack(Activity_Settings.class);
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
        ServiceHandler sh = new ServiceHandler();


        String jsonStr = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Locations/getLastChangedDate?year="+String.valueOf(year), ServiceHandler.GET);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String changed_date = jsonObj.getString(Location_NoeC.KEY_CHANGED_DATE);
                Integer anzahl = jsonObj.getInt("num");
                Log.d("Response: ", "> " + changed_date + " " + String.valueOf(year) + " " + anzahl.toString());
                int updateneeded = db.updateForYearNeeded(year, changed_date, anzahl);
                Log.d("Update neeeded?: ", String.valueOf(updateneeded));
                if (updateneeded == 1) {
                    String putBody = db.getStringAktDates(year);
                    Log.d(TAG, putBody);
                    ServiceHandler shput = new ServiceHandler();
                    String jsonStrakt = shput.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Locations/getChangedDestinationIds", ServiceHandler.PUT, null, putBody);
                    Log.d(TAG, String.valueOf(jsonStrakt));
                    if (jsonStrakt != null) {
                        try {
                            JSONArray nummern = new JSONArray(jsonStrakt);
                            updatewiththisJsondata(nummern);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("ServiceHandler", "Couldn't get any data from the url");
                    }

                }else if(updateneeded == 2){
                    String jsonStrakt = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path)+"Locations/findAllIdsToYear", ServiceHandler.GET);
                    if (jsonStrakt != null) {
                        try {
                            JSONArray nummern = new JSONArray(jsonStrakt);
                            updatewiththisJsondata(nummern);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("ServiceHandler", "Couldn't get any data from the url");
                    }
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
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mBuilder.setContentText("Download complete");
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(2, mBuilder.build());
        mNotifyManager.cancel(2);
    }

    protected void updatewiththisJsondata(JSONArray nummern){
        ServiceHandler sh = new ServiceHandler();
        Integer anzahlakt = nummern.length();
        Integer zael = 1;
        for (int i = 0; i < nummern.length(); i++) {

            try {
                Log.e("Test", String.valueOf(nummern.getInt(i)));

                String jsonLocId = sh.makeServiceCall(mContext.getResources().getString(R.string.api_path) + "Locations/" + String.valueOf(nummern.getInt(i)), ServiceHandler.GET);
                JSONObject jsonLoc = new JSONObject(jsonLocId);
                Log.d(TAG, jsonLoc.toString());
                /*JSONObject destinationsakt = nummern.getJSONObject(i).getJSONObject(TAG_DESTINATION);
                Log.d(TAG, destinationsakt.toString());
                Integer nummer = destinationsakt.getInt(Location_NoeC.KEY_NUMMER);
                Log.d(TAG, nummer.toString());

                Log.d("Hole ID: ", String.valueOf(nummer) + "  Jahr: " + String.valueOf(year));*/
                mBuilder.setProgress(anzahlakt, zael, false);
                mNotifyManager.notify(2, mBuilder.build());
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
            int id = destination.getInt(Location_NoeC.KEY_ID);
            boolean updateornew=db.updateornewForItemNeeded(id);
            if(!updateornew){

                    Location_NoeC newloc = new Location_NoeC();
                    newloc.setId(id);
                    newloc.setNummer(destination.getInt(Location_NoeC.KEY_NUMMER));
                    newloc.setJahr(destination.getInt(Location_NoeC.KEY_JAHR));
                    newloc.setKat(destination.getString(Location_NoeC.KEY_KAT));
                    newloc.setReg(destination.getString(Location_NoeC.KEY_REG));
                    newloc.setName(destination.getString(Location_NoeC.KEY_NAME));
                    newloc.setLatitude(destination.getDouble(Location_NoeC.KEY_LAT));
                    newloc.setLongitude(destination.getDouble(Location_NoeC.KEY_LON));
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
            Location_NoeC newloc = db.getLocationToId(id);
            newloc.setId(destinations.getInt(Location_NoeC.KEY_ID));
            newloc.setNummer(destinations.getInt(Location_NoeC.KEY_NUMMER));
            newloc.setJahr(destinations.getInt(Location_NoeC.KEY_JAHR));
            newloc.setKat(destinations.getString(Location_NoeC.KEY_KAT));
            newloc.setReg(destinations.getString(Location_NoeC.KEY_REG));
            newloc.setName(destinations.getString(Location_NoeC.KEY_NAME));
            newloc.setEmail(destinations.getString(Location_NoeC.KEY_EMAIL));
            newloc.setLatitude(destinations.getDouble(Location_NoeC.KEY_LAT));
            newloc.setLongitude(destinations.getDouble(Location_NoeC.KEY_LON));
            newloc.setAdr_plz(destinations.getString(Location_NoeC.KEY_ADR_PLZ));
            newloc.setTel(destinations.getString(Location_NoeC.KEY_TEL));
            newloc.setFax(destinations.getString(Location_NoeC.KEY_FAX));
            newloc.setAnreise(destinations.getString(Location_NoeC.KEY_ANREISE));
            newloc.setGeoeffnet(destinations.getString(Location_NoeC.KEY_GEOEFFNET));
            newloc.setAdr_ort(destinations.getString(Location_NoeC.KEY_ADR_ORT));
            newloc.setAdr_street(destinations.getString(Location_NoeC.KEY_ADR_STREET));
            newloc.setTipp(destinations.getString(Location_NoeC.KEY_TIPP));
            newloc.setRollstuhl(destinations.getBoolean(Location_NoeC.KEY_ROLLSTUHL));
            newloc.setKinderwagen(destinations.getBoolean(Location_NoeC.KEY_KINDERWAGEN));
            newloc.setHund(destinations.getBoolean(Location_NoeC.KEY_HUND));
            newloc.setGruppe(destinations.getBoolean(Location_NoeC.KEY_GRUPPE));
            newloc.setWebseite(destinations.getString(Location_NoeC.KEY_WEBSEITE));
            newloc.setBeschreibung(destinations.getString(Location_NoeC.KEY_BESCHREIBUNG));
            newloc.setAussersonder(destinations.getString(Location_NoeC.KEY_AUSSERSONDER));
            newloc.setEintritt(destinations.getString(Location_NoeC.KEY_EINTRITT));
            newloc.setErsparnis(destinations.getString(Location_NoeC.KEY_ERSPARNIS));
            newloc.setTop_ausflugsziel(destinations.getBoolean(Location_NoeC.KEY_TOP_AUSFLUGSZIEL));

            newloc.setChanged_date(destinations.getString(Location_NoeC.KEY_CHANGED_DATE));
            newloc.setChange_index(destinations.getInt(Location_NoeC.KEY_CHANGE_INDEX));
            db.updateLocation(newloc);

        } catch (JSONException e) {
            Log.e("Exception3", String.valueOf(e));
            e.printStackTrace();
        }
    }

}
