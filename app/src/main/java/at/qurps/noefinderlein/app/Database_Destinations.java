package at.qurps.noefinderlein.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by roman on 03.04.16.
 */

public class Database_Destinations extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 11;
    public static final String DATABASE_NAME = "NoecardLocationT.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String TEXT_TYPE_0 = " TEXT DEFAULT (null)";
    private static final String INT_TYPE = " INTEGER";
    private static final String INT_TYPE_0 = " INTEGER DEFAULT (0)";

    private static final String BOOL_TYPE_0 = " BOOLEAN DEFAULT (0)";
    private static final String DOUBLE_TYPE_0 = " DOUBLE DEFAULT (0)";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_LOCATIONS =
            "CREATE TABLE " + Location_NoeC.TABLE_NAME + " (" +
                    Location_NoeC.KEY_ID + " INTEGER PRIMARY KEY NOT NULL DEFAULT (0)," +
                    Location_NoeC.KEY_NUMMER + INT_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_JAHR + INT_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_KAT + TEXT_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_REG + TEXT_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_NAME + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_EMAIL + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_LAT + DOUBLE_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_LON + DOUBLE_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_ADR_PLZ + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_TEL + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_FAX + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_ANREISE + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_GEOEFFNET + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_ADR_ORT + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_ADR_STREET + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_TIPP + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_ROLLSTUHL + BOOL_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_KINDERWAGEN + BOOL_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_HUND + BOOL_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_GRUPPE + BOOL_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_WEBSEITE + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_BESCHREIBUNG + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_AUSSERSONDER + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_EINTRITT + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_ERSPARNIS + TEXT_TYPE + COMMA_SEP +
                    Location_NoeC.KEY_TOP_AUSFLUGSZIEL + BOOL_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_CHANGED_DATE + TEXT_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_CHANGE_INDEX + INT_TYPE_0 + COMMA_SEP +
                    Location_NoeC.KEY_FAVORIT + BOOL_TYPE_0 +
            " )";

    private static final String SQL_DELETE_LOCATIONS =
            "DROP TABLE IF EXISTS " + Location_NoeC.TABLE_NAME;
    private static final String SQL_DELETE_CHANGEVAL =
            "DROP TABLE IF EXISTS " + DB_Changeval.TABLE_NAME;

    private static final String SQL_CREATE_VISITED =
            "CREATE TABLE " + Visited_Locations.TABLE_NAME + " (" +
                    Visited_Locations.KEY_ID + " INTEGER PRIMARY KEY NOT NULL DEFAULT (0)," +
                    Visited_Locations.KEY_lOC_ID + INT_TYPE_0 + COMMA_SEP +
                    Visited_Locations.KEY_YEAR + INT_TYPE_0 + COMMA_SEP +
                    Visited_Locations.KEY_LOGGED_DATE + TEXT_TYPE_0 +
                    " )";

    private static final String SQL_CREATE_CHANGEVAL =
            "CREATE TABLE " + DB_Changeval.TABLE_NAME + " (" +
                    DB_Changeval.KEY_YEAR + " INTEGER PRIMARY KEY NOT NULL DEFAULT (0)," +
                    DB_Changeval.KEY_COUNT + INT_TYPE_0 +
                    " )";

    public Database_Destinations(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOCATIONS);
        db.execSQL(SQL_CREATE_VISITED);
        db.execSQL(SQL_CREATE_CHANGEVAL);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_LOCATIONS);
        db.execSQL(SQL_CREATE_LOCATIONS);
        db.execSQL(SQL_DELETE_CHANGEVAL);
        db.execSQL(SQL_CREATE_CHANGEVAL);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
