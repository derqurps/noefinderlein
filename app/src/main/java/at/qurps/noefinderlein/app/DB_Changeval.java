package at.qurps.noefinderlein.app;

/**
 * Created by roman on 10.04.16.
 */
public class DB_Changeval {
    public static final String TABLE_NAME = "changeval";

    public static final String KEY_YEAR = "change_year";
    public static final String KEY_COUNT = "change_count";
    int _year;
    int _count;

    // Empty constructor
    public DB_Changeval(){

    }
    // constructor
    public DB_Changeval(int year, int count){
        this._year = year;
        this._count = count;


    }

    // getting Year
    public int getYear(){
        return this._year;
    }
    // setting year
    public void setYear(int year){
        this._year = year;
    }
    // getting Year
    public int getCount(){
        return this._count;
    }
    // setting year
    public void setCount(int count){
        this._count = count;
    }
}
