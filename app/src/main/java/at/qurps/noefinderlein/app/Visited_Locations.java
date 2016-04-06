package at.qurps.noefinderlein.app;

/**
 * Created by roman on 04.04.16.
 */
public class Visited_Locations {
    public static final String TABLE_NAME = "visited";

    public static final String KEY_ID = "vis_id";
    public static final String KEY_lOC_ID = "vis_locid";
    public static final String KEY_YEAR = "vis_year";
    public static final String KEY_LOGGED_DATE = "vis_loggeddate";


    int _id;
    int _locid;
    int _year;
    String _date;

    // Empty constructor
    public Visited_Locations(){

    }
    // constructor
    public Visited_Locations(int locid, int year, String date){
        this._locid = locid;
        this._year = year;
        this._date = date;


    }

    // getting ID
    public int getId(){
        return this._id;
    }
    // setting ID
    public void setId(int id){
        this._id = id;
    }

    // getting Locid
    public int getLocId(){
        return this._locid;
    }
    // setting Locid
    public void setLocId(int locid){
        this._locid = locid;
    }

    // getting Year
    public int getYear(){
        return this._year;
    }
    // setting year
    public void setYear(int year){
        this._year = year;
    }
    // getting date
    public String getDate(){
        return this._date;
    }
    // setting date
    public void setDate(String date){
        this._date = date;
    }

}
