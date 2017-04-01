package at.qurps.noefinderlein.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by roman on 03.04.16.
 */

public class Database_Destinations extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "NoecardData.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String TEXT_TYPE_0 = " TEXT DEFAULT (null)";
    private static final String INT_TYPE = " INTEGER";
    private static final String INT_TYPE_0 = " INTEGER DEFAULT (0)";

    private static final String BOOL_TYPE_0 = " BOOLEAN DEFAULT (0)";
    private static final String BOOL_TYPE_1 = " BOOLEAN DEFAULT (1)";
    private static final String DOUBLE_TYPE_0 = " DOUBLE DEFAULT (0)";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_LOCATIONS =
            "CREATE TABLE " + DB_Location_NoeC.TABLE_NAME + " (" +
                    DB_Location_NoeC.KEY_ID + " INTEGER PRIMARY KEY NOT NULL DEFAULT (0)," +
                    DB_Location_NoeC.KEY_NUMMER + INT_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_JAHR + INT_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_KAT + INT_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_REG + INT_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_NAME + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_EMAIL + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_LAT + DOUBLE_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_LON + DOUBLE_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_ADR_PLZ + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_TEL + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_FAX + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_ANREISE + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_GEOEFFNET + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_ADR_ORT + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_ADR_STREET + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_TIPP + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_ROLLSTUHL + BOOL_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_KINDERWAGEN + BOOL_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_HUND + BOOL_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_GRUPPE + BOOL_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_WEBSEITE + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_BESCHREIBUNG + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_AUSSERSONDER + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_EINTRITT + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_ERSPARNIS + TEXT_TYPE + COMMA_SEP +
                    DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL + BOOL_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_CHANGED_DATE + TEXT_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_CHANGE_INDEX + INT_TYPE_0 + COMMA_SEP +
                    DB_Location_NoeC.KEY_FAVORIT + BOOL_TYPE_0 +
            " )";

    private static final String SQL_DELETE_LOCATIONS =
            "DROP TABLE IF EXISTS " + DB_Location_NoeC.TABLE_NAME;
    private static final String SQL_DELETE_CHANGEVAL =
            "DROP TABLE IF EXISTS " + DB_Changeval.TABLE_NAME;

    private static final String SQL_CREATE_VISITED =
            "CREATE TABLE " + DB_Visited_Locations.TABLE_NAME + " (" +
                    DB_Visited_Locations.KEY_ID + " INTEGER PRIMARY KEY NOT NULL DEFAULT (0)," +
                    DB_Visited_Locations.KEY_lOC_ID + INT_TYPE_0 + COMMA_SEP +
                    DB_Visited_Locations.KEY_YEAR + INT_TYPE_0 + COMMA_SEP +
                    DB_Visited_Locations.KEY_LOGGED_DATE + TEXT_TYPE_0 +
                    " )";

    private static final String SQL_CREATE_CHANGEVAL =
            "CREATE TABLE " + DB_Changeval.TABLE_NAME + " (" +
                    DB_Changeval.KEY_YEAR + " INTEGER PRIMARY KEY NOT NULL DEFAULT (0)," +
                    DB_Changeval.KEY_COUNT + INT_TYPE_0 +
                    " )";

    private static final String SQL_CREATE_DAYS =
            "CREATE TABLE " + DB_Days.TABLE_NAME + " (" +
                    DB_Days.KEY_DAY+ " TEXT NOT NULL," +
                    DB_Days.KEY_LOCKEY + " INTEGER NOT NULL," +
                    DB_Days.KEY_YEAR + INT_TYPE_0 + COMMA_SEP +
                    DB_Days.KEY_ACTIVE + BOOL_TYPE_1 + COMMA_SEP +
                    DB_Days.KEY_CHANGE + INT_TYPE_0 + "," +
                    "PRIMARY KEY ("+DB_Days.KEY_DAY+", "+DB_Days.KEY_LOCKEY+")" +
                    " )";

    public Database_Destinations(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOCATIONS);
        db.execSQL(SQL_CREATE_VISITED);
        db.execSQL(SQL_CREATE_CHANGEVAL);
        db.execSQL(SQL_CREATE_DAYS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        switch(oldVersion){
            case 1:
            case 2:
                db.execSQL(SQL_DELETE_LOCATIONS);
                db.execSQL(SQL_CREATE_LOCATIONS);
                db.execSQL(SQL_DELETE_CHANGEVAL);
            case 3:
                //add new table for open on day + add new column for noecard website id

                db.execSQL(SQL_CREATE_DAYS);
            case 4:
                db.execSQL("DELETE FROM " + DB_Days.TABLE_NAME + " WHERE year=2016");
            case 5:
                db.execSQL("DELETE FROM " + DB_Days.TABLE_NAME + " WHERE year=2016");
            case 6:
        }
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db, oldVersion, newVersion);
        switch(newVersion){
            case 1:
            case 2:
            case 3:
            case 4:
        }
    }
}
