package at.qurps.noefinderlein.app;

public class DB_Visited_ArrayAdapter {

    DB_Location_NoeC _dbloc;
    DB_Visited_Locations _dbvis;

    // Empty constructor
    public DB_Visited_ArrayAdapter(){

    }
    // constructor
    public DB_Visited_ArrayAdapter(DB_Location_NoeC dbloc, DB_Visited_Locations dbvis){
        this._dbloc = dbloc;
        this._dbvis = dbvis;
    }

    // getting dbloc
    public DB_Location_NoeC getLoc(){
        return this._dbloc;
    }
    // setting dbloc
    public void setLoc(DB_Location_NoeC dbloc){
        this._dbloc = dbloc;
    }

    // getting dbvis
    public DB_Visited_Locations getVis(){
        return this._dbvis;
    }
    // setting dbvis
    public void setVis(DB_Visited_Locations dbvis){
        this._dbvis = dbvis;
    }

}
