package at.qurps.noefinderlein.app;

/**
 * Created by roman on 15.03.17.
 */

public class DB_Days {
    public static final String TABLE_NAME = "days";

    public static final String KEY_DAY = "day";
    public static final String KEY_LOCKEY = "locationId";
    public static final String KEY_YEAR = "year";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_CHANGE = "change_index";

    String _day;
    int _location_id;
    int _year;
    boolean _active;
    int _change_index;

    // Empty constructor
    public DB_Days(){

    }
    // constructor
    public DB_Days(String day, int location_id, int year, boolean active, int change_index){
        this._day = day;
        this._location_id = location_id;
        this._year = year;
        this._active = active;
        this._change_index = change_index;

    }

    // getting Year
    public String getDay(){
        return this._day;
    }
    // setting year
    public void setDay(String day){
        this._day = day;
    }
    public int getLocationId(){
        return this._location_id;
    }

    public void setLocationId(int location_id){
        this._location_id = location_id;
    }

    public int getYear(){
        return this._year;
    }

    public void setYear(int year){
        this._year = year;
    }

    public boolean getActive(){
        return this._active;
    }

    public void setActive(boolean active){
        this._active = active;
    }

    public int getChangeIndex(){
        return this._change_index;
    }

    public void setChangeIndex(int change_index){
        this._change_index = change_index;
    }
}
