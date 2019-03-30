package at.qurps.noefinderlein.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.hypertrack.hyperlog.HyperLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.qurps.noefinderlein.app.APIData.OpenData;


public class DestinationsDB {



    public static final String TAG = "DestinationsDB";
    private Database_Destinations mDbHelper;
    private Context mContext;


    public DestinationsDB(Context context) {
        mContext = context;
        mDbHelper = Database_Destinations.getInstance(context);
    }


    public boolean isVisited(int visitedId){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        boolean returnVar = false;
        Cursor cursor = db.query(DB_Visited_Locations.TABLE_NAME,
                null,
                DB_Visited_Locations.KEY_LOC_ID + "=?",
                new String[]{String.valueOf(visitedId)},
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()){
            returnVar = true;
        }else {
            returnVar = false;
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
        return  returnVar;
    }
    public boolean insertVisitedData(DB_Visited_Locations visited){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String Query = "SELECT " + DB_Visited_Locations.KEY_ID + " FROM " + DB_Visited_Locations.TABLE_NAME + " WHERE " + DB_Visited_Locations.KEY_YEAR + " = " + visited.getYear() + " AND "+ DB_Visited_Locations.KEY_LOC_ID + " = " + visited.getLocId() + " AND " + DB_Visited_Locations.KEY_LOGGED_DATE + " = '" + visited.getDate() +"'";

        Cursor cursor = db.rawQuery(Query, null);
        int count = cursor.getCount();
        if(count == 0) {
            if(cursor != null) {
                cursor.close();
            }
            ContentValues values = new ContentValues();
            values.put(DB_Visited_Locations.KEY_LOC_ID, visited.getLocId());
            values.put(DB_Visited_Locations.KEY_YEAR, visited.getYear());
            values.put(DB_Visited_Locations.KEY_LOGGED_DATE, visited.getDate());
            values.put(DB_Visited_Locations.KEY_SAVED, visited.getSaved());


            // Inserting Row
            db.insert(DB_Visited_Locations.TABLE_NAME, null, values);
            db.close(); // Closing database connection
            return true;
        }else{
            if(cursor != null) {
                cursor.close();
            }
            db.close(); // Closing database connection
            return false;
        }
    }
    // Adding new minimal location
    public void addMinimalLocation(DB_Location_NoeC location) {
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	ContentValues values = new ContentValues();

        values.put(DB_Location_NoeC.KEY_ID, location.getId());
    	values.put(DB_Location_NoeC.KEY_NUMMER, location.getNummer());
        values.put(DB_Location_NoeC.KEY_JAHR, location.getJahr());
    	values.put(DB_Location_NoeC.KEY_KAT,location.getKat());
    	values.put(DB_Location_NoeC.KEY_REG,location.getReg());
    	values.put(DB_Location_NoeC.KEY_NAME,location.getName());
    	values.put(DB_Location_NoeC.KEY_LAT,location.getLatitude());
    	values.put(DB_Location_NoeC.KEY_LON,location.getLongitude());
        values.put(DB_Location_NoeC.KEY_CHANGED_DATE,location.getChanged_date());
        values.put(DB_Location_NoeC.KEY_CHANGE_INDEX,location.getChange_index());

    	// Inserting Row
    	db.insert(DB_Location_NoeC.TABLE_NAME, null, values);
    	db.close(); // Closing database connection
    }



    // Getting single ziel
    public DB_Location_NoeC getLocation(int nummer,int jahr) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(DB_Location_NoeC.TABLE_NAME,
                null,
                DB_Location_NoeC.KEY_NUMMER + "=? AND " + DB_Location_NoeC.KEY_JAHR + "=?",
                new String[]{String.valueOf(nummer), String.valueOf(jahr)},
                null, null, null, null);

        DB_Location_NoeC location;
        if (cursor != null && cursor.moveToFirst()){
            location = getLocationfromCursor(cursor);
        }else {
            location = new DB_Location_NoeC();
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
        return location; // return location
    }

    // Getting single ziel
    public DB_Location_NoeC getLocationToId(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(DB_Location_NoeC.TABLE_NAME,
                null,
                DB_Location_NoeC.KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        DB_Location_NoeC location;
        if (cursor != null && cursor.moveToFirst()){
            location = getLocationfromCursor(cursor);
            location.setTodayActive(isOpenToday(location.getId()));
        }else {
            location = new DB_Location_NoeC();
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
        return location; // return location
    }



    // Getting single ziel
    public DB_Location_NoeC getMinimalLocation(int nummer) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(DB_Location_NoeC.TABLE_NAME,
                new String[]{
                        DB_Location_NoeC.KEY_ID,
                        DB_Location_NoeC.KEY_NUMMER,
                        DB_Location_NoeC.KEY_JAHR,
                        DB_Location_NoeC.KEY_KAT,
                        DB_Location_NoeC.KEY_REG,
                        DB_Location_NoeC.KEY_NAME},
                DB_Location_NoeC.KEY_NUMMER + "=?",
                new String[]{String.valueOf(nummer)},
                null, null, null, null);
        DB_Location_NoeC location = new DB_Location_NoeC();
        if (cursor != null) {
            cursor.moveToFirst();
            location.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID))));
            location.setNummer(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NUMMER))));
            location.setJahr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_JAHR))));
            location.setKat(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KAT)));
            location.setReg(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_REG)));
            location.setName(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NAME)));

            cursor.close();
        }
    	db.close(); // Closing database connection
    	return location; // return location
    }

    // Getting All locations
    public List<DB_Location_NoeC> getAllLocations(int year) {
    	List<DB_Location_NoeC> locationList = new ArrayList<DB_Location_NoeC>();
    	// Select All Query
    	String selectQuery = "SELECT  * FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE "+ DB_Location_NoeC.KEY_JAHR+"="+String.valueOf(year)+" ORDER BY "+DB_Location_NoeC.KEY_NUMMER;

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);

    	// looping through all rows and adding to list
    	if (cursor != null && cursor.moveToFirst()) {
    		do {
    			DB_Location_NoeC location = getLocationfromCursor(cursor);
    			locationList.add(location);
    		} while (cursor.moveToNext());
    	}
        if(cursor != null) {
            cursor.close();
        }
    	db.close(); // Closing database connection
    	return locationList;
    }

    // Getting All favorit locations
    public List<DB_Location_NoeC> getAllFavoritLocations(int year) {
        List<DB_Location_NoeC> locationList = new ArrayList<DB_Location_NoeC>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE "+ DB_Location_NoeC.KEY_FAVORIT +"=1 AND "+DB_Location_NoeC.KEY_JAHR+"="+String.valueOf(year)+" ORDER BY "+DB_Location_NoeC.KEY_NUMMER + "," + DB_Location_NoeC.KEY_NAME;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DB_Location_NoeC location = getLocationfromCursor(cursor);
                locationList.add(location);
            } while (cursor.moveToNext());
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
        return locationList;
    }

    // Getting All locations
    public List<DB_Visited_ArrayAdapter> getAllVisitedLocations(int year) {
        List<DB_Visited_ArrayAdapter> locationList = new ArrayList<DB_Visited_ArrayAdapter>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DB_Visited_Locations.TABLE_NAME + " LEFT JOIN "+DB_Location_NoeC.TABLE_NAME+" ON " + DB_Visited_Locations.TABLE_NAME+"."+ DB_Visited_Locations.KEY_LOC_ID +"="+DB_Location_NoeC.TABLE_NAME+"."+DB_Location_NoeC.KEY_ID+" WHERE "+ DB_Visited_Locations.TABLE_NAME+"."+ DB_Visited_Locations.KEY_YEAR+"="+String.valueOf(year)+ " ORDER BY "+ DB_Visited_Locations.TABLE_NAME+"."+ DB_Visited_Locations.KEY_LOGGED_DATE + " DESC";

        //String selectQuery = "SELECT  * FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE "+ DB_Location_NoeC.KEY_ANGESEHEN +"=1 AND "+DB_Location_NoeC.KEY_JAHR+"="+String.valueOf(year)+" ORDER BY "+DB_Location_NoeC.KEY_NUMMER;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID)) != null) {
                    DB_Visited_ArrayAdapter da = new DB_Visited_ArrayAdapter(getLocationfromCursor(cursor), getVisitedLocationfromCursor(cursor));

                    locationList.add(da);
                }
            } while (cursor.moveToNext());
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
        return locationList;
    }
    // Getting All locations
    public boolean removeVisited(int id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(DB_Visited_Locations.TABLE_NAME, DB_Visited_Locations.KEY_ID + "=" + id, null) > 0;
    }
    //gets all locations to a specified locations String
    public List<DB_Location_NoeC> getAllLocations_toDestIDs(String locationIDs,int year) {
    	String locationIDs_split[]=locationIDs.split(";");
    	List<DB_Location_NoeC> locationList = new ArrayList<DB_Location_NoeC>();
        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
    	String selectQuery = "SELECT  * FROM " + DB_Location_NoeC.TABLE_NAME + " ";
    	for(int i=0;i<locationIDs_split.length;i++)
    	{
    		if(i==0)
    		{
    			selectQuery+=" WHERE (";
    		}
    		else
    		{
    			selectQuery+=" OR ";
    		}
    		selectQuery+=DB_Location_NoeC.KEY_ID+"='"+locationIDs_split[i]+"'";
    	}
    	selectQuery+=") AND "+DB_Location_NoeC.KEY_JAHR+"="+ String.valueOf(year)+" ORDER BY "+DB_Location_NoeC.KEY_NUMMER;

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);

    	// looping through all rows and adding to list
    	if (cursor != null && cursor.moveToFirst()) {
    		do {
    			DB_Location_NoeC location = getLocationfromCursor(cursor);
                location.setTodayActive(isOpenToday(location.getId(), fDate));
    			locationList.add(location);
    		} while (cursor.moveToNext());
    	}
        if(cursor != null) {
            cursor.close();
        }
    	db.close(); // Closing database connection
    	return locationList;
    }
    
    // Getting All Menu locations
    public List<DB_Location_NoeC> getAllMenuLocations(int jahr) {
    	List<DB_Location_NoeC> locationList = new ArrayList<DB_Location_NoeC>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            String fDate = Util.getDBDateString(mContext);


            String dbl = DB_Location_NoeC.TABLE_NAME;
            String dbd = DB_Days.TABLE_NAME;
            String dbv = DB_Visited_Locations.TABLE_NAME;

            Cursor cursor = db.rawQuery("select "
                            + dbl + ".*, case when exists (select * from " + dbd + " where " + dbl + "." + DB_Location_NoeC.KEY_ID + " = " + dbd + "." + DB_Days.KEY_LOCKEY + " and " + DB_Days.KEY_DAY + " = ? and " + dbd + "." + DB_Days.KEY_ACTIVE + " = 1) then 1 else 0 end AS " + DB_Location_NoeC.KEY_GEOEFFNET + ","
                            + " case when exists (select * from " + dbv + " where " + dbl + "." + DB_Location_NoeC.KEY_ID + " = " + dbv + "." + DB_Visited_Locations.KEY_LOC_ID + " and " + DB_Visited_Locations.KEY_YEAR + "=?) then 1 else 0 end AS " + DB_Location_NoeC.KEY_VISITED
                            + " from " + dbl
                            + " where " + DB_Location_NoeC.KEY_JAHR + " = ?"
                            + " order by " + DB_Location_NoeC.KEY_NUMMER + " asc"
                    , new String[]{fDate, String.valueOf(jahr), String.valueOf(jahr)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DB_Location_NoeC location = new DB_Location_NoeC();
                    location.setId(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID)));
                    location.setNummer(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_NUMMER)));
                    location.setJahr(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_JAHR)));
                    location.setName(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NAME)));
                    location.setTop_ausflugsziel(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL))));
                    location.setKat(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KAT)));
                    location.setAdr_ort(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ADR_ORT)));

                    location.setHund(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_HUND))));
                    location.setRollstuhl(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_ROLLSTUHL))));
                    location.setKinderwagen(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KINDERWAGEN))));
                    location.setGruppe(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_GRUPPE))));
                    location.setTodayActive(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_GEOEFFNET))));
                    location.setVisited(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_VISITED))));
                    location.setSearchStr(location.getName() + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_BESCHREIBUNG)) + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_GEOEFFNET)) + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_EINTRITT)));
                    locationList.add(location);
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch(Exception ex) {
            HyperLog.e("DestDB General Exception 1", ex.toString());
        } finally {
            db.close(); // Closing database connection
        }
    	// return location list
    	return locationList;
    }

    // Getting string with actual changeddate
    public String getStringAktDates(int year) {
        JSONObject returnObj = new JSONObject();
        try {
            JSONArray returnArray = new JSONArray();
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Cursor cursor = db.query(DB_Location_NoeC.TABLE_NAME, new String[]{
                            DB_Location_NoeC.KEY_ID,
                            DB_Location_NoeC.KEY_CHANGE_INDEX},
                    DB_Location_NoeC.KEY_JAHR + "=?",
                    new String[]{String.valueOf(year)},
                    null, null, DB_Location_NoeC.KEY_ID + " ASC", null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject newObj = new JSONObject();
                    newObj.put("id", cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID)));
                    newObj.put("cId", cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_CHANGE_INDEX)));
                    returnArray.put(newObj);
                } while (cursor.moveToNext());
                returnObj.put("el",returnArray);
                returnObj.put("year",year);
            }
            else {
                returnObj.put("year",year);
            }
            if(cursor != null) {
                cursor.close();
            }
            db.close();
        }catch (JSONException e) {
            HyperLog.e("DDBERR1: ",  String.valueOf(e) );
            e.printStackTrace();
        }
        return returnObj.toString();
    }
    
    
    // Getting All Menu locations to a region
    public List<DB_Location_NoeC> getAllMenuLocationstoRegion(int regionnumber,int jahr) {
    	List<DB_Location_NoeC> locationList = new ArrayList<DB_Location_NoeC>();


        String fDate = Util.getDBDateString(mContext);

        HyperLog.d("nummer + jahr : ", String.valueOf(regionnumber)+" "+String.valueOf(jahr));
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String dbl = DB_Location_NoeC.TABLE_NAME;
        String dbd = DB_Days.TABLE_NAME;
        String dbv = DB_Visited_Locations.TABLE_NAME;

        Cursor cursor = db.rawQuery("select "
                        + dbl + ".*, case when exists (select * from " + dbd + " where " + dbl + "." + DB_Location_NoeC.KEY_ID + " = " + dbd + "." + DB_Days.KEY_LOCKEY + " and " + DB_Days.KEY_DAY + " = ? and " + dbd + "." + DB_Days.KEY_ACTIVE + " = 1) then 1 else 0 end AS " + DB_Location_NoeC.KEY_GEOEFFNET + ","
                        + " case when exists (select * from " + dbv + " where " + dbl + "." + DB_Location_NoeC.KEY_ID + " = " + dbv + "." + DB_Visited_Locations.KEY_LOC_ID + " and " + DB_Visited_Locations.KEY_YEAR + "=?) then 1 else 0 end AS " + DB_Location_NoeC.KEY_VISITED
                        + " from " + dbl
                        + " where " + DB_Location_NoeC.KEY_REG + " like '%" + String.valueOf(regionnumber) + "%' and " + DB_Location_NoeC.KEY_JAHR + " = ?"
                        + " order by " + DB_Location_NoeC.KEY_ID + " asc"
                , new String[]{fDate, String.valueOf(jahr), String.valueOf(jahr)});

    	// looping through all rows and adding to list
    	if (cursor != null && cursor.moveToFirst()) {
    		do {
    			DB_Location_NoeC location = new DB_Location_NoeC();
                location.setId(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID)));
    			location.setNummer(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_NUMMER)));
                location.setJahr(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_JAHR)));
    			location.setName(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NAME)));
    			location.setTop_ausflugsziel(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL))));
    			location.setKat(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KAT)));
    			location.setAdr_ort(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ADR_ORT)));
                location.setHund(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_HUND))));
                location.setRollstuhl(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_ROLLSTUHL))));
                location.setKinderwagen(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KINDERWAGEN))));
                location.setGruppe(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_GRUPPE))));
                location.setTodayActive(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_GEOEFFNET))));
                location.setVisited(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_VISITED))));
                location.setSearchStr(location.getName() + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_BESCHREIBUNG)) + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_GEOEFFNET)) + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_EINTRITT)));
                locationList.add(location);
    		} while (cursor.moveToNext());
    	}
        if(cursor != null) {
            cursor.close();
        }
    	db.close(); // Closing database connection
    	return locationList;
    }
    
    
 // Getting All Menu locations incl latlong
    public List<DB_Location_NoeC> getAllMenuDistanceLocations(int jahr) {
    	List<DB_Location_NoeC> locationList = new ArrayList<DB_Location_NoeC>();
        String fDate = Util.getDBDateString(mContext);
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String dbl = DB_Location_NoeC.TABLE_NAME;
        String dbd = DB_Days.TABLE_NAME;
        String dbv = DB_Visited_Locations.TABLE_NAME;

        Cursor cursor = db.rawQuery("select "
                        + dbl + ".*, case when exists (select * from " + dbd + " where " + dbl + "." + DB_Location_NoeC.KEY_ID + " = " + dbd + "." + DB_Days.KEY_LOCKEY + " and " + DB_Days.KEY_DAY + " = ? and " + dbd + "." + DB_Days.KEY_ACTIVE + " = 1) then 1 else 0 end AS " + DB_Location_NoeC.KEY_GEOEFFNET + ","
                        + " case when exists (select * from " + dbv + " where " + dbl + "." + DB_Location_NoeC.KEY_ID + " = " + dbv + "." + DB_Visited_Locations.KEY_LOC_ID + " and " + DB_Visited_Locations.KEY_YEAR + "=?) then 1 else 0 end AS " + DB_Location_NoeC.KEY_VISITED
                        + " from " + dbl
                        + " where " + DB_Location_NoeC.KEY_JAHR + " = ?"
                        + " order by " + DB_Location_NoeC.KEY_ID + " asc"
                , new String[]{fDate, String.valueOf(jahr), String.valueOf(jahr)});

        // looping through all rows and adding to list
    	if (cursor != null && cursor.moveToFirst()) {
    		do {
    			DB_Location_NoeC location = new DB_Location_NoeC();
                location.setId(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID)));
    			location.setNummer(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_NUMMER)));
                location.setJahr(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_JAHR)));
    			location.setName(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NAME)));
    			location.setTop_ausflugsziel(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL))));
    			location.setKat(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KAT)));
    			location.setAdr_ort(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ADR_ORT)));
    			location.setLatitude(cursor.getDouble(cursor.getColumnIndex(DB_Location_NoeC.KEY_LAT)));
    			location.setLongitude(cursor.getDouble(cursor.getColumnIndex(DB_Location_NoeC.KEY_LON)));
                location.setHund(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_HUND))));
                location.setRollstuhl(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_ROLLSTUHL))));
                location.setKinderwagen(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KINDERWAGEN))));
                location.setGruppe(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_GRUPPE))));
                location.setTodayActive(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_GEOEFFNET))));
                location.setVisited(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_VISITED))));
                location.setSearchStr(location.getName() + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_BESCHREIBUNG)) + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_GEOEFFNET)) + " " + cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_EINTRITT)));
                locationList.add(location);
    		} while (cursor.moveToNext());
    	}
        if(cursor != null) {
            cursor.close();
        }
    	db.close(); // Closing database connection
    	return locationList;
    }
    // Getting contacts Count
    public int getLocationsCount() {
    	String countQuery = "SELECT  * FROM " + DB_Location_NoeC.TABLE_NAME;
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, null);

    	return cursor.getCount(); // return count
    }
    // Getting contacts Count
    public int getLocationsCountToYear(int year) {
        String countQuery = "SELECT  * FROM " + DB_Location_NoeC.TABLE_NAME+" WHERE "+DB_Location_NoeC.KEY_JAHR +"="+String.valueOf(year);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int returnCount = 0;
        if(cursor != null) {
            returnCount = cursor.getCount();
            cursor.close();
        }
        db.close();
        return returnCount; // return count
    }

    // Getting All locations as Cursor
    public Cursor getAllLocations_asCursor() {
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	Cursor cursor = db.query(DB_Location_NoeC.TABLE_NAME, new String[] {
            DB_Location_NoeC.KEY_ID,
            DB_Location_NoeC.KEY_NUMMER,
            DB_Location_NoeC.KEY_NAME
        }, null,null, null, null, DB_Location_NoeC.KEY_NUMMER + " ASC", null);
    	
    	return cursor;
    }

    public boolean isYearInDatabase(int year) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Boolean result = false;
        Cursor cursor = null;
        try {
            cursor = db.query(DB_Location_NoeC.TABLE_NAME,
                    null,
                    DB_Location_NoeC.KEY_JAHR + "=?",
                    new String[]{String.valueOf(year)},
                    null, null, null, null);

            if (cursor != null && cursor.getCount() > 10) {
                result = true; // return count
            }
        }catch(Exception e) {
            HyperLog.e("DestDB General Exception 2", e.toString());
        } finally {
            if(cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return result;
    }

    public boolean IsLocationInDatabase(int nummer) {
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	Cursor cursor = db.query(DB_Location_NoeC.TABLE_NAME,
                null,
                DB_Location_NoeC.KEY_NUMMER + "=?",
    			new String[] { String.valueOf(nummer) },
                null, null, null, null);
        boolean returnvar = false;
    	if (cursor != null && cursor.getCount()>0)
    	{
            returnvar = true; // return count
            cursor.close();
    	}
    	db.close();
        return returnvar;

    }

    // Updating single location
    public int updateLocation(DB_Location_NoeC location) {
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = getAllContentValuesFromObject(location);

    	// updating row
        int updateInt = db.update(DB_Location_NoeC.TABLE_NAME,
                values,
                DB_Location_NoeC.KEY_ID + " = ? ",
                new String[] { String.valueOf(location.getId()) } );
        db.close();
    	return updateInt;
    }

    //updating Favorit Field
    public int updateFavorit(DB_Location_NoeC location) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_Location_NoeC.KEY_FAVORIT,location.getFavorit());
        int updateInt =db.update(DB_Location_NoeC.TABLE_NAME,
                values,
                DB_Location_NoeC.KEY_ID + " = ? AND " + DB_Location_NoeC.KEY_JAHR + " = ?",
                new String[] { String.valueOf(location.getId()),String.valueOf(location.getJahr()) } );
        db.close();
        return updateInt;
    }

    // Deleting single location
    public void deleteLocation(DB_Location_NoeC location) {
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	db.delete(DB_Location_NoeC.TABLE_NAME,
                DB_Location_NoeC.KEY_NUMMER + " = ? AND " + DB_Location_NoeC.KEY_JAHR + " = ?",
                new String[] { String.valueOf(location.getNummer()),String.valueOf(location.getJahr()) });
    	db.close();
    }
    public void deleteLocationToId(int locationId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(DB_Location_NoeC.TABLE_NAME,
                DB_Location_NoeC.KEY_ID + " = ?",
                new String[] { String.valueOf(locationId) });
        db.close();
    }

    // Deleting single location
    public void delItemsNotInArray(int year, JSONArray numbers) {
        List<Integer> serverList = new ArrayList<Integer>();
        if (numbers != null) {
            int len = numbers.length();
            for (int i = 0; i < len; i++) {
                try {
                    serverList.add(numbers.getInt(i));
                } catch (JSONException e) {
                    HyperLog.e("DestDB General Exception 3", e.toString());
                    e.printStackTrace();
                }
            }
        }
        delItemsNotInArray(year, serverList);
    }
    // Deleting single location
    public void delItemsNotInArray(int year, List<Integer> serverList) {
        String Query = "SELECT " + DB_Location_NoeC.KEY_ID + " FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE " + DB_Location_NoeC.KEY_JAHR + " = " + year + " ORDER BY " + DB_Location_NoeC.KEY_ID + "";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(Query, null);

        // looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID)));
                if(!serverList.contains(id)) {
                    deleteLocationToId(id);
                }
            } while (cursor.moveToNext());
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
    }
    public void removeYear(int year) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DB_Location_NoeC.TABLE_NAME,
                    DB_Location_NoeC.KEY_JAHR + " = ?",
                    new String[] { String.valueOf(year) });
            db.delete(DB_Changeval.TABLE_NAME,
                    DB_Changeval.KEY_YEAR + " = ?",
                    new String[] { String.valueOf(year) });
            db.delete(DB_Days.TABLE_NAME,
                    DB_Days.KEY_YEAR + " = ?",
                    new String[] { String.valueOf(year) });
            db.setTransactionSuccessful();

        } catch(Exception e){
            HyperLog.e("DestDB General Exception 5", e.toString());
        } finally {
            db.endTransaction();
            db.close(); // Closing database connection
        }
    }
    // Delete all location but location in list
    public void deleteAllButArrayLocations(ArrayList<Integer> locationlist) {
    	String[] location_string=new String[locationlist.size()];
    	String deleteQuery = "DELETE FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE ";
    	for(int i = 0; i < locationlist.size(); i++) {
    		location_string[i]=locationlist.get(i).toString();
    		deleteQuery += DB_Location_NoeC.KEY_NUMMER + " != " + locationlist.get(i).toString() ;
    		if ((i+1)<locationlist.size())
    		{
    			deleteQuery += " AND ";
    		}
    	}
    	deleteQuery += "";
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	
    	db.rawQuery(deleteQuery,null);
    	db.close();
    }
    private DB_Visited_Locations getVisitedLocationfromCursor(Cursor cursor) {
        DB_Visited_Locations location = new DB_Visited_Locations();
        location.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Visited_Locations.KEY_ID))));
        location.setLocId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Visited_Locations.KEY_LOC_ID))));
        location.setYear(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Visited_Locations.KEY_YEAR))));
        location.setDate(cursor.getString(cursor.getColumnIndex(DB_Visited_Locations.KEY_LOGGED_DATE)));
        location.setLat((float)cursor.getDouble(cursor.getColumnIndex(DB_Visited_Locations.KEY_LAT)));
        location.setLon((float)cursor.getDouble(cursor.getColumnIndex(DB_Visited_Locations.KEY_LON)));
        location.setSaved((float)cursor.getDouble(cursor.getColumnIndex(DB_Visited_Locations.KEY_SAVED)));

        return location;
    }
    private DB_Location_NoeC getLocationfromCursor(Cursor cursor)
    {
        DB_Location_NoeC location = new DB_Location_NoeC();
        HyperLog.e("ID",  String.valueOf(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID))) );
        location.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ID))));
        location.setNummer(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NUMMER))));
        location.setJahr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_JAHR))));
        location.setChange_index(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_CHANGE_INDEX))));

        location.setKat(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_KAT)));
        location.setReg(cursor.getInt(cursor.getColumnIndex(DB_Location_NoeC.KEY_REG)));
        location.setEmail(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_EMAIL)));
        location.setName(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NAME)));

        location.setNoecIndex(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NOEC_IDX)));
        location.setGooglePlaceId(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_GOOGLE_PLACE_ID)));

        location.setAdr_plz(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ADR_PLZ)));
        location.setTel(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_TEL)));
        location.setFax(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_FAX)));
        location.setAnreise(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ANREISE)));
        location.setGeoeffnet(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_GEOEFFNET)));
        location.setAdr_ort(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ADR_ORT)));
        location.setAdr_street(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ADR_STREET)));
        location.setTipp(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_TIPP)));

        location.setWebseite(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_WEBSEITE)));
        location.setBeschreibung(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_BESCHREIBUNG)));
        location.setAussersonder(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_AUSSERSONDER)));
        location.setEintritt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_EINTRITT)));
        location.setErsparnis(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ERSPARNIS)));
        location.setChanged_date(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_CHANGED_DATE)));

        location.setLatitude(cursor.getDouble(cursor.getColumnIndex(DB_Location_NoeC.KEY_LAT)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndex(DB_Location_NoeC.KEY_LON)));

        location.setRollstuhl(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_ROLLSTUHL)))));
        location.setKinderwagen(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_KINDERWAGEN)))));
        location.setHund(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_HUND)))));
        location.setGruppe(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_GRUPPE)))));
        location.setTop_ausflugsziel(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL)))));
        location.setFavorit(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_FAVORIT)))));

        try{
            location.setVisited_id(cursor.getInt(cursor.getColumnIndex(DB_Visited_Locations.KEY_ID)));
            location.setVisited_date(cursor.getString(cursor.getColumnIndex(DB_Visited_Locations.KEY_LOGGED_DATE)));
        }catch(Exception e){
            HyperLog.e("DDBERR2: ",  String.valueOf(e) );
        }
    	return location;
    }
    public int updateForYearNeeded(int year, int changedcount)
    {
        String Query = "SELECT " + DB_Changeval.KEY_COUNT + " FROM " + DB_Changeval.TABLE_NAME + " WHERE " + DB_Changeval.KEY_YEAR + " = " + year + " AND " + DB_Changeval.KEY_COUNT + " >= "+String.valueOf(changedcount);
        HyperLog.d("DDBQU: ",Query);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        HyperLog.d("WEB_Count: ",  String.valueOf(changedcount) );
        int returnInt = 1;
        if (cursor != null && cursor.moveToFirst()) {
            HyperLog.d("DB_Count: ", String.valueOf(cursor.getInt(cursor.getColumnIndex(DB_Changeval.KEY_COUNT))));
            returnInt = 0;
            cursor.close();
        }
        db.close();
        return returnInt;

    }
    public int getCurrentLastChangeId(int year) {
        String Query = "SELECT " + DB_Days.KEY_CHANGE + " FROM " + DB_Days.TABLE_NAME + " WHERE " + DB_Days.KEY_YEAR + " = " + year + " ORDER BY " + DB_Days.KEY_CHANGE + " DESC LIMIT 1";
        HyperLog.d("DDBQU: ",Query);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        int returnInt = 0;
        if (cursor != null && cursor.moveToFirst()) {
            HyperLog.d("DB_Count: ", String.valueOf(cursor.getInt(cursor.getColumnIndex(DB_Days.KEY_CHANGE))));
            returnInt = cursor.getInt(cursor.getColumnIndex(DB_Days.KEY_CHANGE));
            cursor.close();
        }
        db.close();
        return returnInt;

    }
    public boolean insertOrReplaceDays(JSONArray jsonDays, int year) throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jsonDays.length(); i++) {
                JSONObject dayObj = jsonDays.getJSONObject(i);

                ContentValues values = new ContentValues();
                values.put(DB_Days.KEY_DAY, dayObj.getString("d"));
                values.put(DB_Days.KEY_LOCKEY, dayObj.getInt("l"));
                values.put(DB_Days.KEY_YEAR, year);
                values.put(DB_Days.KEY_ACTIVE, dayObj.getInt("a"));
                values.put(DB_Days.KEY_CHANGE, dayObj.getInt("c"));

                db.replace(DB_Days.TABLE_NAME, null, values);


            }
            db.setTransactionSuccessful();

        } catch(Exception e){
            throw new Exception();
        } finally {
            db.endTransaction();
            db.close(); // Closing database connection
            return true;
        }

    }
    public boolean insertOrReplaceDays(List<OpenData> jsonDays, int year) throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < jsonDays.size(); i++) {
                OpenData dayObj = jsonDays.get(i);

                ContentValues values = new ContentValues();
                values.put(DB_Days.KEY_DAY, dayObj.getD());
                values.put(DB_Days.KEY_LOCKEY, dayObj.getL());
                values.put(DB_Days.KEY_YEAR, year);
                values.put(DB_Days.KEY_ACTIVE, dayObj.getA());
                values.put(DB_Days.KEY_CHANGE, dayObj.getC());

                db.replace(DB_Days.TABLE_NAME, null, values);


            }
            db.setTransactionSuccessful();

        } catch(Exception e){
            throw new Exception();
        } finally {
            db.endTransaction();
            db.close(); // Closing database connection
            return true;
        }

    }
    public void insertOrReplaceLocations(JSONArray jsonDays){

        List<ContentValues> updateList = new ArrayList<ContentValues>();
        List<ContentValues> insertList = new ArrayList<ContentValues>();
        for (int i = 0; i < jsonDays.length(); i++) {
            try {
                JSONObject locationObj = jsonDays.getJSONObject(i);

                int id = locationObj.getInt(DB_Location_NoeC.KEY_ID);
                boolean update = updateornewForItemNeeded(id);
                if (update) {
                    ContentValues values = getAllContentValuesFromObject(getLocationFromJson(locationObj, getLocationToId(id)));
                    updateList.add(values);


                } else {
                    // insert
                    ContentValues values = getAllContentValuesFromObject(getLocationFromJson(locationObj));
                    insertList.add(values);
                }
            } catch(JSONException ex) {
                HyperLog.e("DestDB JSON Exception 10", ex.toString());
            }
        }
        insOrRepl(updateList, insertList);
    }
    public void insertOrReplaceLocations(List<DB_Location_NoeC> jsonDays) {
        List<ContentValues> updateList = new ArrayList<ContentValues>();
        List<ContentValues> insertList = new ArrayList<ContentValues>();
        for (int i = 0; i < jsonDays.size(); i++) {
            DB_Location_NoeC locationObj = jsonDays.get(i);
            int id = locationObj.getId();
            boolean update = updateornewForItemNeeded(id);
            ContentValues values = getAllContentValuesFromObject(locationObj);
            // HyperLog.d(TAG, values.toString());
            if (update) {
                updateList.add(values);
            } else {
                // insert
                insertList.add(values);
            }
        }
        insOrRepl(updateList, insertList);
    }
    private void insOrRepl(List<ContentValues> updateList, List<ContentValues> insertList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < updateList.size(); i++) {
                db.update(DB_Location_NoeC.TABLE_NAME,
                        updateList.get(i),
                        DB_Location_NoeC.KEY_ID + " = ? ",
                        new String[]{String.valueOf(updateList.get(i).get(DB_Location_NoeC.KEY_ID))});
            }
            for (int i = 0; i < insertList.size(); i++) {
                db.insert(DB_Location_NoeC.TABLE_NAME, null, insertList.get(i));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close(); // Closing database connection
        }
    }
    private ContentValues getAllContentValuesFromObject(DB_Location_NoeC location) {
        ContentValues values = new ContentValues();

        values.put(DB_Location_NoeC.KEY_ID,location.getId());
        values.put(DB_Location_NoeC.KEY_NUMMER, location.getNummer());
        values.put(DB_Location_NoeC.KEY_JAHR, location.getJahr());
        values.put(DB_Location_NoeC.KEY_KAT,location.getKat());
        values.put(DB_Location_NoeC.KEY_REG,location.getReg());
        values.put(DB_Location_NoeC.KEY_NAME, location.getName());
        values.put(DB_Location_NoeC.KEY_EMAIL, location.getEmail());
        values.put(DB_Location_NoeC.KEY_LAT,location.getLatitude());
        values.put(DB_Location_NoeC.KEY_LON,location.getLongitude());
        values.put(DB_Location_NoeC.KEY_ADR_PLZ,location.getAdr_plz());
        values.put(DB_Location_NoeC.KEY_TEL,location.getTel());
        values.put(DB_Location_NoeC.KEY_FAX,location.getFax());
        values.put(DB_Location_NoeC.KEY_ANREISE,location.getAnreise());
        values.put(DB_Location_NoeC.KEY_GEOEFFNET,location.getGeoeffnet());
        values.put(DB_Location_NoeC.KEY_ADR_ORT,location.getAdr_ort());
        values.put(DB_Location_NoeC.KEY_ADR_STREET,location.getAdr_street());
        values.put(DB_Location_NoeC.KEY_TIPP, location.getTipp());
        values.put(DB_Location_NoeC.KEY_ROLLSTUHL,location.getRollstuhl());
        values.put(DB_Location_NoeC.KEY_KINDERWAGEN,location.getKinderwagen());
        values.put(DB_Location_NoeC.KEY_HUND, location.getHund());
        values.put(DB_Location_NoeC.KEY_GRUPPE,location.getGruppe());
        values.put(DB_Location_NoeC.KEY_WEBSEITE,location.getWebseite());
        values.put(DB_Location_NoeC.KEY_BESCHREIBUNG,location.getBeschreibung());
        values.put(DB_Location_NoeC.KEY_AUSSERSONDER,location.getAussersonder());
        values.put(DB_Location_NoeC.KEY_EINTRITT,location.getEintritt());
        values.put(DB_Location_NoeC.KEY_ERSPARNIS,location.getErsparnis());
        values.put(DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL,location.getTop_ausflugsziel());
        values.put(DB_Location_NoeC.KEY_CHANGED_DATE,location.getChanged_date());
        values.put(DB_Location_NoeC.KEY_CHANGE_INDEX,location.getChange_index());
        values.put(DB_Location_NoeC.KEY_NOEC_IDX,location.getNoecIndex());
        values.put(DB_Location_NoeC.KEY_GOOGLE_PLACE_ID,location.getGooglePlaceId());
        return values;
    }
    private DB_Location_NoeC getLocationFromJson(JSONObject location) {
        return getLocationFromJson(location, null);
    }

    private DB_Location_NoeC getLocationFromJson(JSONObject location, DB_Location_NoeC newloc) {
        if(newloc == null){
            newloc = new DB_Location_NoeC();
        }
        try {
            newloc.setId(location.getInt(DB_Location_NoeC.KEY_ID));
            newloc.setNummer(location.getInt(DB_Location_NoeC.KEY_NUMMER));
            newloc.setJahr(location.getInt(DB_Location_NoeC.KEY_JAHR));
            newloc.setKat(location.getInt(DB_Location_NoeC.KEY_KAT));
            newloc.setReg(location.getInt(DB_Location_NoeC.KEY_REG));
            newloc.setName(location.getString(DB_Location_NoeC.KEY_NAME));
            newloc.setEmail(location.getString(DB_Location_NoeC.KEY_EMAIL));
            newloc.setLatitude(location.getDouble(DB_Location_NoeC.KEY_LAT));
            newloc.setLongitude(location.getDouble(DB_Location_NoeC.KEY_LON));
            newloc.setAdr_plz(location.getString(DB_Location_NoeC.KEY_ADR_PLZ));
            newloc.setTel(location.getString(DB_Location_NoeC.KEY_TEL));
            newloc.setFax(location.getString(DB_Location_NoeC.KEY_FAX));
            newloc.setAnreise(location.getString(DB_Location_NoeC.KEY_ANREISE));
            newloc.setGeoeffnet(location.getString(DB_Location_NoeC.KEY_GEOEFFNET));
            newloc.setAdr_ort(location.getString(DB_Location_NoeC.KEY_ADR_ORT));
            newloc.setAdr_street(location.getString(DB_Location_NoeC.KEY_ADR_STREET));
            newloc.setTipp(location.getString(DB_Location_NoeC.KEY_TIPP));
            newloc.setRollstuhl(location.getBoolean(DB_Location_NoeC.KEY_ROLLSTUHL));
            newloc.setKinderwagen(location.getBoolean(DB_Location_NoeC.KEY_KINDERWAGEN));
            newloc.setHund(location.getBoolean(DB_Location_NoeC.KEY_HUND));
            newloc.setGruppe(location.getBoolean(DB_Location_NoeC.KEY_GRUPPE));
            newloc.setWebseite(location.getString(DB_Location_NoeC.KEY_WEBSEITE));
            newloc.setBeschreibung(location.getString(DB_Location_NoeC.KEY_BESCHREIBUNG));
            newloc.setAussersonder(location.getString(DB_Location_NoeC.KEY_AUSSERSONDER));
            newloc.setEintritt(location.getString(DB_Location_NoeC.KEY_EINTRITT));
            newloc.setErsparnis(location.getString(DB_Location_NoeC.KEY_ERSPARNIS));
            newloc.setTop_ausflugsziel(location.getBoolean(DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL));

            newloc.setNoecIndex(location.getString(DB_Location_NoeC.KEY_NOEC_IDX));
            newloc.setGooglePlaceId(location.getString(DB_Location_NoeC.KEY_GOOGLE_PLACE_ID));

            newloc.setChanged_date(location.getString(DB_Location_NoeC.KEY_CHANGED_DATE));
            newloc.setChange_index(location.getInt(DB_Location_NoeC.KEY_CHANGE_INDEX));


        } catch (JSONException e) {
            HyperLog.e("JSON Exception3", String.valueOf(e));
            e.printStackTrace();
        }
        return newloc;
    }


    public int updateChangeId(int year, int changedcount) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DB_Changeval.KEY_YEAR, year);
        values.put(DB_Changeval.KEY_COUNT, changedcount);

        int id = (int) db.insertWithOnConflict(DB_Changeval.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update(DB_Changeval.TABLE_NAME, values, DB_Changeval.KEY_YEAR+"=?", new String[] {String.valueOf(year)});
        }
        db.close();
        // updating row
        return 1;
    }
    public boolean updateornewForItemNeeded(int id)
    {
        String Query = "SELECT " + DB_Location_NoeC.KEY_ID + " FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE " + DB_Location_NoeC.KEY_ID + " = " + id + "";
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query,null);
        //HyperLog.d("cursor: ",DatabaseUtils.dumpCursorToString(cursor));
        int count = 0;
        count=cursor.getCount();
        //HyperLog.d("updateornew: ",  String.valueOf(count) );
        cursor.close();
        db.close();
        if (count>0) {
            return true;
        }
        else {
            return false;
        }

    }
    public boolean areNumbersAvailable(int year){
        int countpyear = getLocationsCountToYear(year);
        String Query = "SELECT " + DB_Location_NoeC.KEY_ID + " FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE " + DB_Location_NoeC.KEY_JAHR + " = " + String.valueOf(year) + " AND "+DB_Location_NoeC.KEY_NUMMER+"!=0";
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query,null);
        //HyperLog.d("cursor: ",DatabaseUtils.dumpCursorToString(cursor));
        int count = 0;
        count=cursor.getCount();
        cursor.close();
        db.close();
        if(count != countpyear){
            return false;
        }
        else{
            return true;
        }
    }
    private boolean getBooleanfromInt(int x)
    {
    	return x == 1;
    }
    public boolean isOpenToday(int locId) {
        String fDate = Util.getDBDateString(mContext);
        return isOpenToday(locId, fDate);
    }

    public boolean isOpenToday(int locId, String day) {

        boolean returnVal = false;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + DB_Days.TABLE_NAME + " WHERE "+DB_Days.KEY_DAY+"=? AND "+DB_Days.KEY_LOCKEY+"=? AND " + DB_Days.KEY_ACTIVE + "=1", new String[]{day, String.valueOf(locId)});

        if (mCursor != null && mCursor.moveToFirst())
        {
            returnVal = true;
            mCursor.close();
        }
        db.close();
        //HyperLog.d("isOpen: ", String.valueOf(locId) + " " + day + " " + String.valueOf(returnVal));
        return returnVal;
    }
    public float getSavingsToYear(int year, boolean filterAccepted) {
        float ersparnis = 0;

        String query = "SELECT " + DB_Visited_Locations.KEY_SAVED + " FROM " + DB_Visited_Locations.TABLE_NAME + " WHERE " + DB_Visited_Locations.KEY_YEAR+"=" + String.valueOf(year) + (filterAccepted ? " AND " + DB_Visited_Locations.KEY_ACCEPTED+"=1" : "");

        HyperLog.d(TAG, String.valueOf(query));
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{});

        if (cursor != null && cursor.moveToFirst()) {
            HyperLog.d(TAG,String.valueOf(cursor.getCount()));
            do {
                try{
                    ersparnis = ersparnis + cursor.getFloat(cursor.getColumnIndex(DB_Visited_Locations.KEY_SAVED));
                }
                catch (Exception e) {
                    HyperLog.e(TAG,e.toString());
                }

            } while (cursor.moveToNext());
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection

        return ersparnis;
    }
    public int getVisitedLocationsCount(int year, boolean filterAccepted) {
        return getVisitedLocationsCount(year, filterAccepted, false);
    }
    public int getVisitedLocationsCount(int year, boolean filterAccepted, boolean distinct) {
        int count = 0;
        String query = "SELECT " + (distinct ? "DISTINCT ":"") + DB_Visited_Locations.KEY_LOC_ID + " FROM " + DB_Visited_Locations.TABLE_NAME + "  WHERE " + DB_Visited_Locations.TABLE_NAME + "."+DB_Visited_Locations.KEY_YEAR+"=" + String.valueOf(year) + (filterAccepted ? " AND " + DB_Visited_Locations.TABLE_NAME + "." + DB_Visited_Locations.KEY_ACCEPTED+"=1" : "");

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{});
        if(cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        db.close(); // Closing database connection
        return count;
    }
    public int getVisitedTOPLocationsCount(int year, boolean filterAccepted, boolean distinct) {
        int count = 0;
        String query = "SELECT " + (distinct ? "DISTINCT ":"") + DB_Visited_Locations.TABLE_NAME + "." + DB_Visited_Locations.KEY_LOC_ID + " AS " + DB_Visited_Locations.KEY_LOC_ID + " FROM " + DB_Visited_Locations.TABLE_NAME + " LEFT JOIN " + DB_Location_NoeC.TABLE_NAME + " ON " + DB_Visited_Locations.TABLE_NAME + "." + DB_Visited_Locations.KEY_LOC_ID + " = " + DB_Location_NoeC.TABLE_NAME + "." + DB_Location_NoeC.KEY_ID + " WHERE " + DB_Visited_Locations.TABLE_NAME + "."+DB_Visited_Locations.KEY_YEAR+"=" + String.valueOf(year) + " AND " + DB_Location_NoeC.TABLE_NAME + "."+DB_Location_NoeC.KEY_TOP_AUSFLUGSZIEL+"=1" + (filterAccepted ? " AND " + DB_Visited_Locations.TABLE_NAME + "." + DB_Visited_Locations.KEY_ACCEPTED+"=1" : "");

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{});
        if(cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        db.close(); // Closing database connection
        return count;
    }

    public int getMaxOnOneDayVisitedLocationsCount(int year, boolean filterAccepted, boolean distinct) {
        int count = 0;
        String query = "SELECT count(" + DB_Visited_Locations.KEY_LOGGED_DATE + ") as anz, " + DB_Visited_Locations.KEY_LOGGED_DATE + " FROM (SELECT " + DB_Visited_Locations.KEY_LOGGED_DATE + ", " + DB_Visited_Locations.KEY_LOC_ID + " FROM " + DB_Visited_Locations.TABLE_NAME + " WHERE " + DB_Visited_Locations.KEY_YEAR + "=" + String.valueOf(year) + " GROUP BY " + DB_Visited_Locations.KEY_LOC_ID + ", " + DB_Visited_Locations.KEY_LOGGED_DATE + " ) GROUP BY " + DB_Visited_Locations.KEY_LOGGED_DATE + " ORDER BY anz DESC LIMIT 1";
        HyperLog.d(TAG, String.valueOf(query));
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{});
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex("anz"));
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
        return count;
    }
    public int getNumToId(int id) {
        int num = 0;
        String query = "SELECT " + DB_Location_NoeC.KEY_NUMMER + " FROM " + DB_Location_NoeC.TABLE_NAME + " WHERE " + DB_Location_NoeC.KEY_ID + "=" + String.valueOf(id);
        HyperLog.d(TAG, String.valueOf(query));
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{});
        if (cursor != null && cursor.moveToFirst()) {
            num = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DB_Location_NoeC.KEY_NUMMER)));
        }
        if(cursor != null) {
            cursor.close();
        }
        db.close(); // Closing database connection
        return num;
    }

}