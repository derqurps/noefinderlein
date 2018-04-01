package at.qurps.noefinderlein.app;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;



/**
 * Created by roman on 08.04.17.
 */

public class AccomplishmentsOutbox {

    private DestinationsDB mDb;

    private boolean filterAccepted = false;

    private boolean mfirstLocation = false;
    private boolean m3in1 = false;
    private boolean m7in1 = false;


    private int m5locations = 0;
    private int m10locations = 0;
    private int m50locations = 0;
    private int malllocations = 0;
    private int mtoplocations = 0;

    private double mScoreSavings = -1;
    private int mScoreCount = -1;

    Context mContext;
    private int myear;

    AccomplishmentsOutbox(Context context, int year, DestinationsDB db) {
        this.mContext = context;
        this.myear = year;
        this.mDb = db;

        switch(year){
            case 2017:
                break;
        }
    }
    boolean isEmpty() {
        switch(myear){
            case 2017:
                return !mfirstLocation && !m3in1 && !m7in1 &&
                        m5locations == 0 && m10locations == 0 &&
                        m50locations == 0 && malllocations == 0 &&
                        mtoplocations == 0 && mScoreSavings < 0 && mScoreCount < 0;
            default:
                return true;
        }

    }

    public void saveLocal() {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
    }

    public void loadLocal() {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
    }

    void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isGameSignedIn()) {
            Toast.makeText(mContext, mContext.getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }
    private boolean isGameSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(mContext) != null;
    }

    public void pushAccomplishments(int id) {
        if (!isGameSignedIn()) {
            // can't push to the cloud, so save locally
            this.saveLocal();
            return;
        }
        switch(myear) {
            case 2017:
                pushAccomplishments2017();
                break;
            case 2018:
                pushAccomplishments2018();
                break;
        }
        this.saveLocal();
    }

    private void pushAccomplishments2017() {
        if (this.mfirstLocation) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .unlock(mContext.getString(R.string.achievement_2017_first_location));
            this.mfirstLocation = false;
        }
        if (this.m3in1) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .unlock(mContext.getString(R.string.achievement_2017_3in1));
            this.m3in1 = false;
        }
        if (this.m7in1) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .unlock(mContext.getString(R.string.achievement_2017_7in1));
            this.m7in1 = false;
        }

        if(this.malllocations != 0){
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2017_all), this.malllocations);
            this.malllocations = 0;
        }
        if (this.m50locations != 0) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2017_50), this.m50locations);
            this.m50locations = 0;
        }
        if (this.m10locations != 0) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2017_10), this.m10locations);
            this.m10locations = 0;
        }
        if (this.m5locations != 0) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2017_5), this.m5locations);
            this.m5locations = 0;
        }
        if(this.mtoplocations != 0){
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2017_top), this.mtoplocations);
            this.mtoplocations = 0;
        }

        if (this.mScoreCount >= 0) {
            Games.getLeaderboardsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .submitScore(mContext.getString(R.string.leaderboard_2017_count), this.mScoreCount);
            this.mScoreCount = -1;
        }
        if (this.mScoreSavings >= 0) {
            Games.getLeaderboardsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .submitScore(mContext.getString(R.string.leaderboard_2017_savings), (long)(this.mScoreSavings*1000000));
            this.mScoreSavings = -1;
        }
    }
    private void pushAccomplishments2018() {
        if (this.mfirstLocation) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .unlock(mContext.getString(R.string.achievement_2018_first_location));
            this.mfirstLocation = false;
        }
        if (this.m3in1) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .unlock(mContext.getString(R.string.achievement_2018_3in1));
            this.m3in1 = false;
        }
        if (this.m7in1) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .unlock(mContext.getString(R.string.achievement_2018_7in1));
            this.m7in1 = false;
        }

        if(this.malllocations != 0){
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2018_all), this.malllocations);
            this.malllocations = 0;
        }
        if (this.m50locations != 0) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2018_50), this.m50locations);
            this.m50locations = 0;
        }
        if (this.m10locations != 0) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2018_10), this.m10locations);
            this.m10locations = 0;
        }
        if (this.m5locations != 0) {
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2018_5), this.m5locations);
            this.m5locations = 0;
        }
        if(this.mtoplocations != 0){
            Games.getAchievementsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .setSteps(mContext.getString(R.string.achievement_2018_top), this.mtoplocations);
            this.mtoplocations = 0;
        }

        if (this.mScoreCount >= 0) {
            Games.getLeaderboardsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .submitScore(mContext.getString(R.string.leaderboard_2018_count), this.mScoreCount);
            this.mScoreCount = -1;
        }
        if (this.mScoreSavings >= 0) {
            Games.getLeaderboardsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                    .submitScore(mContext.getString(R.string.leaderboard_2018_savings), (long)(this.mScoreSavings*1000000));
            this.mScoreSavings = -1;
        }
    }
    void updateLeaderboards(int id) {
        int countOfVisited = mDb.getVisitedLocationsCount(myear, filterAccepted);
        sendEvent(mContext.getString(R.string.event_location_visited_count), countOfVisited);
        if(id!=0) {
            sendEvent(mContext.getString(R.string.event_location_visited_id), id);
            sendEvent(mContext.getString(R.string.event_location_visited_num), mDb.getNumToId(id));
        }
        if (this.mScoreCount < countOfVisited) {
            this.mScoreCount = countOfVisited;
        }
        double savings = mDb.getSavingsToYear(myear,filterAccepted);
        if (this.mScoreSavings < savings) {
            this.mScoreSavings = savings;
        }
    }
    void checkForAchievements(int id) {
        switch(myear) {
            case 2017:
                checkForAchievements2017();
                break;
        }

    }
    void checkForAchievements2017( ) {
        int countOfUniqueVisited = mDb.getVisitedLocationsCount(myear, filterAccepted, true);
        int countOfMaxinOneDay = mDb.getMaxOnOneDayVisitedLocationsCount(myear, filterAccepted, true);

        int countOfTOPUnique = mDb.getVisitedTOPLocationsCount(myear, filterAccepted, true);

        if (countOfUniqueVisited>=1) {
            this.mfirstLocation = true;
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_first_location_text));
        }
        if (countOfMaxinOneDay>=7) {
            this.m7in1 = true;
            this.m3in1 = true;
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_7in1_text));
        } else if (countOfMaxinOneDay>=3) {
            this.m3in1 = true;
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_3in1_text));
        }

        if(countOfUniqueVisited>0){
            this.malllocations = countOfUniqueVisited;
            this.m50locations = countOfUniqueVisited;
            this.m10locations = countOfUniqueVisited;
            this.m5locations = countOfUniqueVisited;
        }
        if (countOfUniqueVisited>=312) {
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_all_text));
        } else if (countOfUniqueVisited>=50) {
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_50_text));
        } else if (countOfUniqueVisited>=10) {
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_10_text));
        } else if (countOfUniqueVisited>=5) {
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_5_text));
        }

        if (countOfTOPUnique>0) {
            this.mtoplocations = countOfTOPUnique;
            // achievementToast(mGoogleApiClient, mContext.getString(R.string.achievement_2017_top_text));
        }
    }
    void sendEvent(String eventId, int pushnum) {
        if (!isGameSignedIn()) {
            return;
        }
        Games.getEventsClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .increment(eventId, pushnum);
    }
}
