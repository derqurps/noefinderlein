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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypertrack.hyperlog.HyperLog;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import at.qurps.noefinderlein.app.APIData.CurrentIds;
import at.qurps.noefinderlein.app.APIData.NoefinderleinAPI;
import at.qurps.noefinderlein.app.APIData.OpenData;
import at.qurps.noefinderlein.app.APIData.TLSSocketFactory;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by roman on 12.04.14.
 */
public class Downloader_Destination_v2 extends AsyncTask<Integer, String, Void> {

    // JSON Node names
    private static final String TAG_JQUERY = "jquery";
    private static final String TAG_DESTINATION = "Destination";
    private static final String TAG_NUMMERN = "nummern";

    public static final String TAG = "Downloader_Destination_v2";

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    String NOTIFICATION_CHANNEL_ID = "downloader_channel_id_01";

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
    private NoefinderleinAPI noefinderleinAPI;

    public Downloader_Destination_v2(Context context, Activity_Main acti) {
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
        createNoefinderleinAPI();
    }

    @Override
    protected Void doInBackground(Integer...pParams) {
        int year = pParams[0];

        boolean loadOpenData = this.prefs.getBoolean(Activity_Settings.KEY_PREF_LOAD_OPEN_DATA, false);

        try {
            CurrentIds yearData = noefinderleinAPI.loadChanges(year).execute().body();

            assert yearData != null;
            HyperLog.d("Response: ", "> " + String.valueOf(yearData.getChangeId()) + " " + String.valueOf(yearData.getDaysChangeId()) + " " + String.valueOf(year));
            int updateneeded = db.updateForYearNeeded(year, yearData.getChangeId());
            int currentChangeIdInDB = db.getCurrentLastChangeId(year);
            HyperLog.d("Update neeeded?: ", String.valueOf(updateneeded));

            if (updateneeded == 1 ) {
                mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
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


                String putBody = db.getStringAktDates(year);
                HyperLog.d(TAG, putBody);

                RequestBody body = RequestBody.create(JSON, putBody);

                List<Integer> changedLocationIds = noefinderleinAPI.getChangedDestinationIds(body).execute().body();

                assert changedLocationIds != null;
                HyperLog.d(TAG, changedLocationIds.toString());

                updatewiththisJsondata(changedLocationIds);

                db.updateChangeId(year, yearData.getChangeId());

            }
            HyperLog.d("Day Update neeeded?: ", String.valueOf(loadOpenData) +" "+ String.valueOf(currentChangeIdInDB) +" "+ String.valueOf(yearData.getDaysChangeId()) +" "+ String.valueOf(currentChangeIdInDB < yearData.getDaysChangeId()));
            if(loadOpenData && currentChangeIdInDB < yearData.getDaysChangeId()) {
                int downloadChangeAnz = yearData.getDaysChangeCount();
                int anzPackages = (downloadChangeAnz / dayPkgCount) + 1;
                HyperLog.d(TAG, " gesamt und anzahl an x packages " + String.valueOf(downloadChangeAnz) + " " +String.valueOf(anzPackages)  );
                progress.setMax(downloadChangeAnz);
                progress.setProgress(0);

                publishProgress("Updating opening days  ...");
                downloadSegment(year, yearData.getDaysChangeId(), anzPackages, 0, 0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            HyperLog.e("IOException 1", e.toString());
        }
        try {
            List<Integer> yearIds = noefinderleinAPI.findAllIdsToYear(year).execute().body();
            assert yearIds != null;
            if(yearIds.size() > 3) {
                db.delItemsNotInArray(year, yearIds);
            }
        } catch (IOException e) {
            e.printStackTrace();
            HyperLog.e("IOException 2", e.toString());
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

    private void downloadSegment(int year, int completeEndChangeId, int pkgCount, int progressC, int counter) {
        int beginsegment = db.getCurrentLastChangeId(year);
        HyperLog.d(TAG, "counter:" + String.valueOf(counter) + " pkgCount:" + String.valueOf(pkgCount) + " beginsegment:" + String.valueOf(beginsegment) + " completeEndChangeId:" + String.valueOf(completeEndChangeId));
        if(counter < (pkgCount + 20) && beginsegment < completeEndChangeId) {
            try {
                List<OpenData> changedLocationIds = noefinderleinAPI.getChangeSegmentCount(year, beginsegment, dayPkgCount).execute().body();
                assert changedLocationIds != null;
                HyperLog.d(TAG, " got anz changes " + String.valueOf(changedLocationIds.size()) );
                int inserted = updateOrInsertJsondata(changedLocationIds, year);
                progress.setProgress(progressC + inserted);
                HyperLog.d(TAG, String.valueOf(year) + " " +String.valueOf(completeEndChangeId) + " " +String.valueOf(pkgCount) + " " +String.valueOf((progressC + inserted)) + " " + String.valueOf((counter + 1)) );
                downloadSegment(year, completeEndChangeId, pkgCount, (progressC + inserted), (counter + 1));
            } catch (IOException e) {
                e.printStackTrace();
                HyperLog.e("IOException 3", e.toString());
            } catch (Exception e) {
                e.printStackTrace();
                HyperLog.e("general Exception 3", e.toString());
            }
        }
    }
    private int updateOrInsertJsondata(List<OpenData> jsonDay, int year) throws Exception {
        if (jsonDay.size() > 0) {
            String currentDay = jsonDay.get(0).getD();

            publishProgress("Updating opening days ...\n" + currentDay);
            db.insertOrReplaceDays(jsonDay, year);
        } else {
            HyperLog.d(TAG, "List Size = 0");
        }
        return jsonDay.size();
    }
    private void updatewiththisJsondata(List<Integer> nummern){

        Integer anzahlakt = nummern.size();
        Integer zael = 1;
        progress.setMax(anzahlakt);

        List<String> idList = new ArrayList<String>();

        for(int i=0;i<anzahlakt;i++) {
            idList.add(String.valueOf(nummern.get(i)));
        }
        int partitionSize = 20;
        List<List<String>> partitions = new LinkedList<List<String>>();
        for (int i = 0; i < anzahlakt; i += partitionSize) {
            partitions.add(idList.subList(i,
                    Math.min(i + partitionSize, idList.size())));
        }
        for (int i = 0; i < partitions.size(); i++) {
            String putBody = "{\"arr\":[" + TextUtils.join(",", partitions.get(i)) + "]}";
            HyperLog.d(TAG, putBody);
            RequestBody body = RequestBody.create(JSON, putBody);
            List<DB_Location_NoeC> changedLocationIds = null;
            try {
                changedLocationIds = noefinderleinAPI.getLocationsToIds(body).execute().body();
                db.insertOrReplaceLocations(changedLocationIds);
                assert changedLocationIds != null;
                zael = zael + changedLocationIds.size();
                mBuilder.setProgress(anzahlakt, zael, false);
                mNotifyManager.notify(2, mBuilder.build());
                progress.setProgress(zael);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNoefinderleinAPI() {


        OkHttpClient client=new OkHttpClient();
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            client = new OkHttpClient.Builder()
                    .sslSocketFactory(new TLSSocketFactory(), trustManager)
                    .build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mContext.getResources().getString(R.string.api_path))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        noefinderleinAPI = retrofit.create(NoefinderleinAPI.class);
    }

}
