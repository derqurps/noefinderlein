package at.qurps.noefinderlein.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DestinationsDB {




    private Database_Destinations mDbHelper;


    public DestinationsDB(Context context) {
        mDbHelper = new Database_Destinations(context);
    }

    public boolean insertVisitedData(Visited_Locations visited){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String Query = "SELECT " + Visited_Locations.KEY_ID + " FROM " + Visited_Locations.TABLE_NAME + " WHERE " + Visited_Locations.KEY_YEAR + " = " + visited.getYear() + " AND "+ Visited_Locations.KEY_lOC_ID + " = " + visited.getLocId() + " AND " + Visited_Locations.KEY_LOGGED_DATE + " = '" + visited.getDate() +"'";

        Cursor cursor = db.rawQuery(Query, null);
        int count = cursor.getCount();
        if(count == 0) {
            ContentValues values = new ContentValues();
            values.put(Visited_Locations.KEY_lOC_ID, visited.getLocId());
            values.put(Visited_Locations.KEY_YEAR, visited.getYear());
            values.put(Visited_Locations.KEY_LOGGED_DATE, visited.getDate());

            // Inserting Row
            db.insert(Visited_Locations.TABLE_NAME, null, values);
            db.close(); // Closing database connection
            return true;
        }else{
            db.close(); // Closing database connection
            return false;
        }
    }
    // Adding new minimal location
    public void addMinimalLocation(Location_NoeC location) {
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	ContentValues values = new ContentValues();

        values.put(Location_NoeC.KEY_ID, location.getId());
    	values.put(Location_NoeC.KEY_NUMMER, location.getNummer());
        values.put(Location_NoeC.KEY_JAHR, location.getJahr());
    	values.put(Location_NoeC.KEY_KAT,location.getKat());
    	values.put(Location_NoeC.KEY_REG,location.getReg());
    	values.put(Location_NoeC.KEY_NAME,location.getName());
    	values.put(Location_NoeC.KEY_LAT,location.getLatitude());
    	values.put(Location_NoeC.KEY_LON,location.getLongitude());
        values.put(Location_NoeC.KEY_CHANGED_DATE,location.getChanged_date());
        values.put(Location_NoeC.KEY_CHANGE_INDEX,location.getChange_index());

    	// Inserting Row
    	db.insert(Location_NoeC.TABLE_NAME, null, values);
    	db.close(); // Closing database connection
    }



    // Getting single ziel
    public Location_NoeC getLocation(int nummer,int jahr) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(Location_NoeC.TABLE_NAME,
                null,
                Location_NoeC.KEY_NUMMER + "=? AND " + Location_NoeC.KEY_JAHR + "=?",
                new String[]{String.valueOf(nummer), String.valueOf(jahr)},
                null, null, null, null);

        Location_NoeC location;
        if (cursor != null && cursor.moveToFirst()){
            location = getLocationfromCursor(cursor);
            cursor.close();
        }else {
            location = new Location_NoeC();
        }
        db.close(); // Closing database connection
        return location; // return location
    }

    // Getting single ziel
    public Location_NoeC getLocationToId(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(Location_NoeC.TABLE_NAME,
                null,
                Location_NoeC.KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        Location_NoeC location;
        if (cursor != null && cursor.moveToFirst()){
            location = getLocationfromCursor(cursor);
            cursor.close();
        }else {
            location = new Location_NoeC();
        }
        db.close(); // Closing database connection
        return location; // return location
    }



    // Getting single ziel
    public Location_NoeC getMinimalLocation(int nummer) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(Location_NoeC.TABLE_NAME,
                new String[]{
                        Location_NoeC.KEY_ID,
                        Location_NoeC.KEY_NUMMER,
                        Location_NoeC.KEY_JAHR,
                        Location_NoeC.KEY_KAT,
                        Location_NoeC.KEY_REG,
                        Location_NoeC.KEY_NAME},
                Location_NoeC.KEY_NUMMER + "=?",
                new String[]{String.valueOf(nummer)},
                null, null, null, null);
        Location_NoeC location = new Location_NoeC();
        if (cursor != null) {
            cursor.moveToFirst();
            location.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ID))));
            location.setNummer(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_NUMMER))));
            location.setJahr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_JAHR))));
            location.setKat(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KAT)));
            location.setReg(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_REG)));
            location.setName(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_NAME)));

            cursor.close();
        }
    	db.close(); // Closing database connection
    	return location; // return location
    }

    // Getting All locations
    public List<Location_NoeC> getAllLocations(int year) {
    	List<Location_NoeC> locationList = new ArrayList<Location_NoeC>();
    	// Select All Query
    	String selectQuery = "SELECT  * FROM " + Location_NoeC.TABLE_NAME + " WHERE "+ Location_NoeC.KEY_JAHR+"="+String.valueOf(year)+" ORDER BY "+Location_NoeC.KEY_NUMMER;

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);

    	// looping through all rows and adding to list
    	if (cursor.moveToFirst()) {
    		do {
    			Location_NoeC location = getLocationfromCursor(cursor);
    			locationList.add(location);
    		} while (cursor.moveToNext());
    	}
    	db.close(); // Closing database connection
        cursor.close();
    	return locationList;
    }

    // Getting All favorit locations
    public List<Location_NoeC> getAllFavoritLocations(int year) {
        List<Location_NoeC> locationList = new ArrayList<Location_NoeC>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + Location_NoeC.TABLE_NAME + " WHERE "+ Location_NoeC.KEY_FAVORIT +"=1 AND "+Location_NoeC.KEY_JAHR+"="+String.valueOf(year)+" ORDER BY "+Location_NoeC.KEY_NUMMER;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Location_NoeC location = getLocationfromCursor(cursor);
                locationList.add(location);
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        cursor.close();
        return locationList;
    }

    // Getting All locations
    public List<Location_NoeC> getAllVisitedLocations(int year) {
        List<Location_NoeC> locationList = new ArrayList<Location_NoeC>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + Visited_Locations.TABLE_NAME + " LEFT JOIN "+Location_NoeC.TABLE_NAME+" ON " +Visited_Locations.TABLE_NAME+"."+Visited_Locations.KEY_lOC_ID+"="+Location_NoeC.TABLE_NAME+"."+Location_NoeC.KEY_ID+" WHERE "+Visited_Locations.TABLE_NAME+"."+Visited_Locations.KEY_YEAR+"="+String.valueOf(year)+ " ORDER BY "+Visited_Locations.TABLE_NAME+"."+Visited_Locations.KEY_LOGGED_DATE + " DESC";

        //String selectQuery = "SELECT  * FROM " + Location_NoeC.TABLE_NAME + " WHERE "+ Location_NoeC.KEY_ANGESEHEN +"=1 AND "+Location_NoeC.KEY_JAHR+"="+String.valueOf(year)+" ORDER BY "+Location_NoeC.KEY_NUMMER;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Location_NoeC location = getLocationfromCursor(cursor);
                locationList.add(location);
            } while (cursor.moveToNext());
        }
        db.close(); // Closing database connection
        cursor.close();
        return locationList;
    }
    // Getting All locations
    public boolean removeVisited(int id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(Visited_Locations.TABLE_NAME, Visited_Locations.KEY_ID + "=" + id, null) > 0;
    }
    //gets all locations to a specified locations String
    public List<Location_NoeC> getAllLocations_toDestIDs(String locationIDs,int year) {
    	String locationIDs_split[]=locationIDs.split(";");
    	List<Location_NoeC> locationList = new ArrayList<Location_NoeC>();

    	String selectQuery = "SELECT  * FROM " + Location_NoeC.TABLE_NAME + " ";
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
    		selectQuery+=Location_NoeC.KEY_ID+"='"+locationIDs_split[i]+"'";
    	}
    	selectQuery+=") AND "+Location_NoeC.KEY_JAHR+"="+ String.valueOf(year)+" ORDER BY "+Location_NoeC.KEY_NUMMER;

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);

    	// looping through all rows and adding to list
    	if (cursor.moveToFirst()) {
    		do {
    			Location_NoeC location = getLocationfromCursor(cursor);
    			locationList.add(location);
    		} while (cursor.moveToNext());
    	}
    	db.close(); // Closing database connection
        cursor.close();
    	return locationList;
    }
    
    // Getting All Menu locations
    public List<Location_NoeC> getAllMenuLocations(int jahr) {
    	List<Location_NoeC> locationList = new ArrayList<Location_NoeC>();

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	Cursor cursor = db.query(Location_NoeC.TABLE_NAME, new String[]{
                        Location_NoeC.KEY_ID,
                        Location_NoeC.KEY_NUMMER,
                        Location_NoeC.KEY_JAHR,
                        Location_NoeC.KEY_NAME,
                        Location_NoeC.KEY_TOP_AUSFLUGSZIEL,
                        Location_NoeC.KEY_KAT,
                        Location_NoeC.KEY_ADR_ORT,
                        Location_NoeC.KEY_HUND,
                        Location_NoeC.KEY_ROLLSTUHL,
                        Location_NoeC.KEY_KINDERWAGEN,
                        Location_NoeC.KEY_GRUPPE},
                Location_NoeC.KEY_JAHR + "=?",
                new String[]{String.valueOf(jahr)},
                null, null, Location_NoeC.KEY_NUMMER + " ASC", null);
        //Log.d("DB_Cursor : ", DatabaseUtils.dumpCursorToString(cursor));
    	// looping through all rows and adding to list
    	if (cursor.moveToFirst()) {
    		do {
    			Location_NoeC location = new Location_NoeC();
                location.setId(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_ID)));
    			location.setNummer(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_NUMMER)));
                location.setJahr(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_JAHR)));
    			location.setName(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_NAME)));
    			location.setTop_ausflugsziel(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_TOP_AUSFLUGSZIEL))));
    			location.setKat(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KAT)));
    			location.setAdr_ort(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ADR_ORT)));

                location.setHund(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_HUND))));
                location.setRollstuhl(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_ROLLSTUHL))));
                location.setKinderwagen(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KINDERWAGEN))));
                location.setGruppe(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_GRUPPE))));
    			locationList.add(location);
    		} while (cursor.moveToNext());
    	}
    	//cursor.close();
    	db.close(); // Closing database connection
    	// return location list
        cursor.close();
    	return locationList;
    }

    // Getting string with actual changeddate
    public String getStringAktDates(int year) {
        JSONObject returnObj = new JSONObject();
        try {
            JSONArray returnArray = new JSONArray();
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Cursor cursor = db.query(Location_NoeC.TABLE_NAME, new String[]{
                            Location_NoeC.KEY_ID,
                            Location_NoeC.KEY_CHANGE_INDEX},
                    Location_NoeC.KEY_JAHR + "=?",
                    new String[]{String.valueOf(year)},
                    null, null, Location_NoeC.KEY_ID + " ASC", null);

            if (cursor.moveToFirst()) {
                do {
                    JSONObject newObj = new JSONObject();
                    newObj.put("id", cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ID)));
                    newObj.put("cId", cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_CHANGE_INDEX)));
                    returnArray.put(newObj);
                } while (cursor.moveToNext());
                returnObj.put("el",returnArray);
                returnObj.put("year",year);
            }
            else {
                returnObj.put("year",year);
            }
            db.close();
            cursor.close();
        }catch (JSONException e) {
            Log.d("DDBERR1: ",  String.valueOf(e) );
            e.printStackTrace();
        }
        return returnObj.toString();
    }
    
    
    // Getting All Menu locations to a region
    public List<Location_NoeC> getAllMenuLocationstoRegion(int regionnumber,int jahr) {
    	List<Location_NoeC> locationList = new ArrayList<Location_NoeC>();
        Log.d("nummer + jahr : ", String.valueOf(regionnumber)+" "+String.valueOf(jahr));
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	Cursor cursor = db.query(Location_NoeC.TABLE_NAME, new String[] {
                    Location_NoeC.KEY_ID,
                    Location_NoeC.KEY_NUMMER,
                    Location_NoeC.KEY_JAHR,
                    Location_NoeC.KEY_NAME,
                    Location_NoeC.KEY_TOP_AUSFLUGSZIEL,
                    Location_NoeC.KEY_KAT,
                    Location_NoeC.KEY_ADR_ORT,
                    Location_NoeC.KEY_HUND,
                    Location_NoeC.KEY_ROLLSTUHL,
                    Location_NoeC.KEY_KINDERWAGEN,
                    Location_NoeC.KEY_GRUPPE
                },
                Location_NoeC.KEY_REG +" LIKE '%"+String.valueOf(regionnumber)+"%' AND " + Location_NoeC.KEY_JAHR + "=?",
                new String[] { String.valueOf(jahr) },
                null, null, Location_NoeC.KEY_NUMMER + " ASC", null);

    	// looping through all rows and adding to list
    	if (cursor.moveToFirst()) {
    		do {
    			Location_NoeC location = new Location_NoeC();
                location.setId(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_ID)));
    			location.setNummer(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_NUMMER)));
                location.setJahr(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_JAHR)));
    			location.setName(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_NAME)));
    			location.setTop_ausflugsziel(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_TOP_AUSFLUGSZIEL))));
    			location.setKat(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KAT)));
    			location.setAdr_ort(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ADR_ORT)));
                location.setHund(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_HUND))));
                location.setRollstuhl(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_ROLLSTUHL))));
                location.setKinderwagen(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KINDERWAGEN))));
                location.setGruppe(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_GRUPPE))));
    			locationList.add(location);
    		} while (cursor.moveToNext());
    	}
    	db.close(); // Closing database connection
        cursor.close();
    	return locationList;
    }
    
    
 // Getting All Menu locations incl latlong
    public List<Location_NoeC> getAllMenuDistanceLocations(int jahr) {
    	List<Location_NoeC> locationList = new ArrayList<Location_NoeC>();

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	Cursor cursor = db.query(Location_NoeC.TABLE_NAME, new String[] {
                    Location_NoeC.KEY_ID,
                    Location_NoeC.KEY_NUMMER,
                    Location_NoeC.KEY_JAHR,
                    Location_NoeC.KEY_NAME,
                    Location_NoeC.KEY_TOP_AUSFLUGSZIEL,
                    Location_NoeC.KEY_KAT,
                    Location_NoeC.KEY_ADR_ORT,
                    Location_NoeC.KEY_LAT,
                    Location_NoeC.KEY_LON,
                    Location_NoeC.KEY_HUND,
                    Location_NoeC.KEY_ROLLSTUHL,
                    Location_NoeC.KEY_KINDERWAGEN,
                    Location_NoeC.KEY_GRUPPE
                },
                Location_NoeC.KEY_JAHR+"=?",
                new String[] {String.valueOf(jahr)},
                null, null, Location_NoeC.KEY_NUMMER + " ASC", null);

    	// looping through all rows and adding to list
    	if (cursor.moveToFirst()) {
    		do {
    			Location_NoeC location = new Location_NoeC();
                location.setId(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_ID)));
    			location.setNummer(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_NUMMER)));
                location.setJahr(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_JAHR)));
    			location.setName(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_NAME)));
    			location.setTop_ausflugsziel(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_TOP_AUSFLUGSZIEL))));
    			location.setKat(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KAT)));
    			location.setAdr_ort(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ADR_ORT)));
    			location.setLatitude(cursor.getDouble(cursor.getColumnIndex(Location_NoeC.KEY_LAT)));
    			location.setLongitude(cursor.getDouble(cursor.getColumnIndex(Location_NoeC.KEY_LON)));
                location.setHund(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_HUND))));
                location.setRollstuhl(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_ROLLSTUHL))));
                location.setKinderwagen(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KINDERWAGEN))));
                location.setGruppe(getBooleanfromInt(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_GRUPPE))));
    			locationList.add(location);
    		} while (cursor.moveToNext());
    	}
    	db.close(); // Closing database connection
        cursor.close();
    	return locationList;
    }
    // Getting contacts Count
    public int getLocationsCount() {
    	String countQuery = "SELECT  * FROM " + Location_NoeC.TABLE_NAME;
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, null);

    	return cursor.getCount(); // return count
    }

    // Getting All locations as Cursor
    public Cursor getAllLocations_asCursor() {
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	Cursor cursor = db.query(Location_NoeC.TABLE_NAME, new String[] {
                Location_NoeC.KEY_ID,
                Location_NoeC.KEY_NUMMER,
                Location_NoeC.KEY_NAME
            }, null,null, null, null, Location_NoeC.KEY_NUMMER + " ASC", null);
    	
    	return cursor;
    }

    public boolean isYearInDatabase(int year) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(Location_NoeC.TABLE_NAME,
                null,
                Location_NoeC.KEY_JAHR + "=?",
                new String[] { String.valueOf(year) },
                null, null, null, null);
        Boolean result = false;
        if (cursor.getCount()>10)
        {
            result = true; // return count
        }
        db.close();
        return result;
    }

    public boolean IsLocationInDatabase(int nummer) {
    	SQLiteDatabase db = mDbHelper.getReadableDatabase();
    	Cursor cursor = db.query(Location_NoeC.TABLE_NAME,
                null,
                Location_NoeC.KEY_NUMMER + "=?",
    			new String[] { String.valueOf(nummer) },
                null, null, null, null);
    	if (cursor.getCount()>0)
    	{
    		return true; // return count
    	}
    	else
    	{
    		return false; // return count
    	}
    }

    // Updating single location
    public int updateLocation(Location_NoeC location) {
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	ContentValues values = new ContentValues();
        values.put(Location_NoeC.KEY_ID,location.getId());
    	values.put(Location_NoeC.KEY_KAT,location.getKat());
    	values.put(Location_NoeC.KEY_REG,location.getReg());
    	values.put(Location_NoeC.KEY_NAME, location.getName());
    	values.put(Location_NoeC.KEY_EMAIL, location.getEmail());
    	values.put(Location_NoeC.KEY_LAT,location.getLatitude());
    	values.put(Location_NoeC.KEY_LON,location.getLongitude());
    	values.put(Location_NoeC.KEY_ADR_PLZ,location.getAdr_plz());
    	values.put(Location_NoeC.KEY_TEL,location.getTel());
    	values.put(Location_NoeC.KEY_FAX,location.getFax());
    	values.put(Location_NoeC.KEY_ANREISE,location.getAnreise());
    	values.put(Location_NoeC.KEY_GEOEFFNET,location.getGeoeffnet());
    	values.put(Location_NoeC.KEY_ADR_ORT,location.getAdr_ort());
    	values.put(Location_NoeC.KEY_ADR_STREET,location.getAdr_street());
    	values.put(Location_NoeC.KEY_TIPP, location.getTipp());
    	values.put(Location_NoeC.KEY_ROLLSTUHL,location.getRollstuhl());
    	values.put(Location_NoeC.KEY_KINDERWAGEN,location.getKinderwagen());
    	values.put(Location_NoeC.KEY_HUND, location.getHund());
    	values.put(Location_NoeC.KEY_GRUPPE,location.getGruppe());
    	values.put(Location_NoeC.KEY_WEBSEITE,location.getWebseite());
    	values.put(Location_NoeC.KEY_BESCHREIBUNG,location.getBeschreibung());
    	values.put(Location_NoeC.KEY_AUSSERSONDER,location.getAussersonder());
    	values.put(Location_NoeC.KEY_EINTRITT,location.getEintritt());
    	values.put(Location_NoeC.KEY_ERSPARNIS,location.getErsparnis());
        values.put(Location_NoeC.KEY_TOP_AUSFLUGSZIEL,location.getTop_ausflugsziel());
        values.put(Location_NoeC.KEY_CHANGED_DATE,location.getChanged_date());
        values.put(Location_NoeC.KEY_CHANGE_INDEX,location.getChange_index());
        values.put(Location_NoeC.KEY_FAVORIT,location.getFavorit());
        
    	// updating row
    	return db.update(Location_NoeC.TABLE_NAME,
                values,
                Location_NoeC.KEY_ID + " = ? ",
    			new String[] { String.valueOf(location.getId()) } );
    }

    //updating Favorit Field
    public int updateFavorit(Location_NoeC location) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Location_NoeC.KEY_FAVORIT,location.getFavorit());

        return db.update(Location_NoeC.TABLE_NAME,
                values,
                Location_NoeC.KEY_NUMMER + " = ? AND " + Location_NoeC.KEY_JAHR + " = ?",
                new String[] { String.valueOf(location.getNummer()),String.valueOf(location.getJahr()) } );
    }

    // Deleting single location
    public void deleteLocation(Location_NoeC location) {
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	db.delete(Location_NoeC.TABLE_NAME,
                Location_NoeC.KEY_NUMMER + " = ? AND " + Location_NoeC.KEY_JAHR + " = ?",
                new String[] { String.valueOf(location.getNummer()),String.valueOf(location.getJahr()) });
    	db.close();
    }
    
    // Delete all location but location in list
    public void deleteAllButArrayLocations(ArrayList<Integer> locationlist) {
    	String[] location_string=new String[locationlist.size()];
    	String deleteQuery = "DELETE FROM " + Location_NoeC.TABLE_NAME + " WHERE ";
    	for(int i = 0; i < locationlist.size(); i++) {
    		location_string[i]=locationlist.get(i).toString();
    		deleteQuery += Location_NoeC.KEY_NUMMER + " != " + locationlist.get(i).toString() ;
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

    private Location_NoeC getLocationfromCursor(Cursor cursor)
    {
        Location_NoeC location = new Location_NoeC();
        location.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ID))));
        location.setNummer(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_NUMMER))));
        location.setJahr(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_JAHR))));
        location.setChange_index(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_CHANGE_INDEX))));

        location.setKat(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_KAT)));
        location.setReg(cursor.getInt(cursor.getColumnIndex(Location_NoeC.KEY_REG)));
        location.setEmail(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_EMAIL)));
        location.setName(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_NAME)));

        location.setAdr_plz(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ADR_PLZ)));
        location.setTel(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_TEL)));
        location.setFax(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_FAX)));
        location.setAnreise(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ANREISE)));
        location.setGeoeffnet(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_GEOEFFNET)));
        location.setAdr_ort(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ADR_ORT)));
        location.setAdr_street(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ADR_STREET)));
        location.setTipp(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_TIPP)));

        location.setWebseite(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_WEBSEITE)));
        location.setBeschreibung(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_BESCHREIBUNG)));
        location.setAussersonder(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_AUSSERSONDER)));
        location.setEintritt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_EINTRITT)));
        location.setErsparnis(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ERSPARNIS)));
        location.setChanged_date(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_CHANGED_DATE)));

        location.setLatitude(cursor.getDouble(cursor.getColumnIndex(Location_NoeC.KEY_LAT)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndex(Location_NoeC.KEY_LON)));

        location.setRollstuhl(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_ROLLSTUHL)))));
        location.setKinderwagen(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_KINDERWAGEN)))));
        location.setHund(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_HUND)))));
        location.setGruppe(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_GRUPPE)))));
        location.setTop_ausflugsziel(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_TOP_AUSFLUGSZIEL)))));
        location.setFavorit(getBooleanfromInt(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Location_NoeC.KEY_FAVORIT)))));

        try{
            location.setVisited_id(cursor.getInt(cursor.getColumnIndex(Visited_Locations.KEY_ID)));
            location.setVisited_date(cursor.getString(cursor.getColumnIndex(Visited_Locations.KEY_LOGGED_DATE)));
        }catch(Exception e){
            Log.d("DDBERR2: ",  String.valueOf(e) );
        }
    	return location;
    }
    public int updateForYearNeeded(int year, int changedcount)
    {
        String Query = "SELECT " + DB_Changeval.KEY_COUNT + " FROM " + DB_Changeval.TABLE_NAME + " WHERE " + DB_Changeval.KEY_YEAR + " = " + year + " AND " + DB_Changeval.KEY_COUNT + " >= "+String.valueOf(changedcount);
        Log.d("DDBQU: ",Query);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        Log.d("WEB_Count: ",  String.valueOf(changedcount) );
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("DB_Count: ", String.valueOf(cursor.getInt(cursor.getColumnIndex(DB_Changeval.KEY_COUNT))));
            return 0;
        }
        else
        {
            return 1;
        }
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
        // updating row
        return 1;
    }
    public boolean updateornewForItemNeeded(int id)
    {
        String Query = "SELECT " + Location_NoeC.KEY_ID + " FROM " + Location_NoeC.TABLE_NAME + " WHERE " + Location_NoeC.KEY_ID + " = " + id + "";
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query,null);
        //Log.d("cursor: ",DatabaseUtils.dumpCursorToString(cursor));
        int count = 0;
        count=cursor.getCount();
        //Log.d("updateornew: ",  String.valueOf(count) );
        cursor.close();
        if (count>0) {
            return true;
        }
        else {
            return false;
        }

    }

    private boolean getBooleanfromInt(int x)
    {
    	return x == 1;
    }
}
