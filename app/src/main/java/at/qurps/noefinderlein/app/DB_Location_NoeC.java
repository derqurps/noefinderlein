package at.qurps.noefinderlein.app;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class DB_Location_NoeC implements ClusterItem {

	public static final String TABLE_NAME = "location";

	public static final String KEY_ID = "id";
	public static final String KEY_NUMMER = "nummer";
	public static final String KEY_JAHR = "jahr";
	public static final String KEY_KAT = "kat";
	public static final String KEY_REG = "reg";
	public static final String KEY_NAME = "name";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_LAT = "latitude";
	public static final String KEY_LON = "longitude";
	public static final String KEY_ADR_PLZ = "adr_plz";
	public static final String KEY_TEL = "tel";
	public static final String KEY_FAX = "fax";
	public static final String KEY_ANREISE = "anreise";
	public static final String KEY_GEOEFFNET = "geoeffnet";
	public static final String KEY_ADR_ORT = "adr_ort";
	public static final String KEY_ADR_STREET = "adr_street";
	public static final String KEY_TIPP = "tipp";
	public static final String KEY_ROLLSTUHL = "rollstuhl";
	public static final String KEY_KINDERWAGEN = "kinderwagen";
	public static final String KEY_HUND = "hund";
	public static final String KEY_GRUPPE = "gruppe";
	public static final String KEY_WEBSEITE = "webseite";
	public static final String KEY_BESCHREIBUNG = "beschreibung";
	public static final String KEY_AUSSERSONDER = "aussersonder";
	public static final String KEY_EINTRITT = "eintritt";
	public static final String KEY_ERSPARNIS = "ersparnis";
	public static final String KEY_TOP_AUSFLUGSZIEL = "top_ausflugsziel";
	public static final String KEY_CHANGED_DATE = "changed_date";
	public static final String KEY_CHANGE_INDEX = "change_index";
	public static final String KEY_NOEC_IDX = "noecardwebsiteid";
    public static final String KEY_GOOGLE_PLACE_ID = "googleplaceid";
	public static final String KEY_FAVORIT = "favorit";
	public static final String KEY_VISITED = "visited";

	//private variables
	private int id;
	private int nummer;
	private int jahr;
	private int kat;
	private int reg;
	private String name;
	private String email;
	private double latitude;
	private double longitude;
	private String adr_plz;
	private String tel;
	private String fax;
	private String anreise;
	private String geoeffnet;
	private String adr_ort;
	private String adr_street;
	private String tipp;
	private boolean rollstuhl;
	private boolean kinderwagen;
	private boolean hund;
	private boolean gruppe;
	private String webseite;
	private String beschreibung;
	private String aussersonder;
	private String eintritt;
	private String ersparnis;
	private boolean top_ausflugsziel;
	private int change_index;
	private String changed_date;
	private String noecardwebsiteid;
	private String googleplaceid;

	private boolean _favorit;


	private double _distance;
	private String _visited_date;
	private int _visited_id;
	private boolean _todayActive;
	private boolean _visited;
	private String _searchstr;
	
	
	// Empty constructor
	public DB_Location_NoeC(){

	}
	// constructor
	public DB_Location_NoeC(int id,
						 int nummer,
						 int jahr,
						 int kat,
						 int reg,
						 String name,
						 String email,
						 float latitude,
						 float longitude,
						 String adr_plz,
						 String tel,
						 String fax,
						 String anreise,
						 String geoeffnet,
						 String adr_ort,
						 String adr_street,
						 String tipp,
						 boolean rollstuhl,
						 boolean kinderwagen,
						 boolean hund,
						 boolean gruppe,
						 String webseite,
						 String beschreibung,
						 String aussersonder,
						 String eintritt,
						 String ersparnis,
						 boolean top_ausflugsziel,
						 String changed_date,
						 int change_index,
						 boolean favorit){
        this.id= id;
		this.nummer = nummer;
        this.jahr = jahr;
		this.kat=kat;
		this.reg=reg;
		this.name=name;
		this.email=email;
		this.latitude=latitude;
		this.longitude=longitude;
		this.adr_plz=adr_plz;
		this.tel=tel;
		this.fax=fax;
		this.anreise=anreise;
		this.geoeffnet=geoeffnet;
		this.adr_ort=adr_ort;
		this.adr_street=adr_street;
		this.tipp=tipp;
		this.rollstuhl=rollstuhl;
		this.kinderwagen=kinderwagen;
		this.hund=hund;
		this.gruppe=gruppe;
		this.webseite=webseite;
		this.beschreibung=beschreibung;
		this.aussersonder=aussersonder;
		this.eintritt=eintritt;
		this.ersparnis=ersparnis;
		this.top_ausflugsziel=top_ausflugsziel;
        this.change_index=change_index;
        this.changed_date=changed_date;
        this._favorit=favorit;
		
	}

	// constructor
	public DB_Location_NoeC(int id,
						 int nummer,
						 int jahr,
						 int kat,
						 int reg,
						 String name){
		this.id= id;
		this.nummer = nummer;
        this.jahr = jahr;
		this.kat=kat;
		this.reg=reg;
		this.name=name;
	}
    // getting ID
    public int getId(){
        return this.id;
    }
    // setting ID
    public void setId(int id){
        this.id = id;
    }

    // getting Nummer
	public int getNummer(){
	    return this.nummer;
	}
	// setting Nummer
	public void setNummer(int nummer){
	    this.nummer = nummer;
	}

    // getting Jahr
    public int getJahr(){
        return this.jahr;
    }
    // setting Nummer
    public void setJahr(int jahr){
        this.jahr = jahr;
    }

	
	// getting kat
	public int getKat(){
		return this.kat;
	}
	// setting kat
	public void setKat(int kat){
		this.kat = kat;
	}
	// getting reg
	public int getReg(){
		return this.reg;
	}
	// setting reg
	public void setReg(int reg){
		this.reg = reg;
	}
	// getting name
	public String getName(){
		return this.name;
	}
	// setting name
	public void setName(String name){
		this.name = name;
	}
	// getting email
	public String getEmail(){
		return this.email;
	}
	// setting email
	public void setEmail(String email){
		this.email = email;
	}
	// getting latitude
	public double getLatitude(){
		return this.latitude;
	}
	// setting latitude
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	// getting longitude
	public double getLongitude(){
		return this.longitude;
	}
	// setting longitude
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	// getting adr_plz
	public String getAdr_plz(){
		return this.adr_plz;
	}
	// setting adr_plz
	public void setAdr_plz(String adr_plz){
		this.adr_plz = adr_plz;
	}
	// getting tel
	public String getTel(){
		return this.tel;
	}
	// setting tel
	public void setTel(String tel){
		this.tel = tel;
	}
	// getting fax
	public String getFax(){
		return this.fax;
	}
	// setting fax
	public void setFax(String fax){
		this.fax = fax;
	}
	// getting anreise
	public String getAnreise(){
		return this.anreise;
	}
	// setting anreise
	public void setAnreise(String anreise){
		this.anreise = anreise;
	}
	// getting geoeffnet
	public String getGeoeffnet(){
		return this.geoeffnet;
	}
	// setting geoeffnet
	public void setGeoeffnet(String geoeffnet){
		this.geoeffnet = geoeffnet;
	}
	// getting adr_ort
	public String getAdr_ort(){
		return this.adr_ort;
	}
	// setting adr_ort
	public void setAdr_ort(String adr_ort){
		this.adr_ort = adr_ort;
	}
	// getting adr_street
	public String getAdr_street(){
		return this.adr_street;
	}
	// setting adr_street
	public void setAdr_street(String adr_street){
		this.adr_street = adr_street;
	}
	// getting tipp
	public String getTipp(){
		return this.tipp;
	}
	// setting tipp
	public void setTipp(String tipp){
		this.tipp = tipp;
	}
	// getting rollstuhl
	public boolean getRollstuhl(){
		return this.rollstuhl;
	}
	// setting rollstuhl
	public void setRollstuhl(boolean rollstuhl){
		this.rollstuhl = rollstuhl;
	}
	// getting kinderwagen
	public boolean getKinderwagen(){
		return this.kinderwagen;
	}
	// setting kinderwagen
	public void setKinderwagen(boolean kinderwagen){
		this.kinderwagen = kinderwagen;
	}
	// getting hund
	public boolean getHund(){
		return this.hund;
	}
	// setting hund
	public void setHund(boolean hund){
		this.hund = hund;
	}
	// getting gruppe
	public boolean getGruppe(){
		return this.gruppe;
	}
	// setting gruppe
	public void setGruppe(boolean gruppe){
		this.gruppe = gruppe;
	}

	// getting webseite
	public String getWebseite(){
		return this.webseite;
	}
	// setting webseite
	public void setWebseite(String webseite){
		this.webseite = webseite;
	}

	// getting beschreibung
	public String getBeschreibung(){
		return this.beschreibung;
	}
	// setting beschreibung
	public void setBeschreibung(String beschreibung){
		this.beschreibung = beschreibung;
	}
	// getting aussersonder
	public String getAussersonder(){
		return this.aussersonder;
	}
	// setting aussersonder
	public void setAussersonder(String aussersonder){
		this.aussersonder = aussersonder;
	}
	// getting eintritt
	public String getEintritt(){
		return this.eintritt;
	}
	// setting eintritt
	public void setEintritt(String eintritt){
		this.eintritt = eintritt;
	}
	// getting ersparnis
	public String getErsparnis(){
		return this.ersparnis;
	}
	// setting ersparnis
	public void setErsparnis(String ersparnis){
		this.ersparnis = ersparnis;
	}
	// getting top_ausflugsziel
	public boolean getTop_ausflugsziel(){
		return this.top_ausflugsziel;
	}
	// setting top_ausflugsziel
	public void setTop_ausflugsziel(boolean top_ausflugsziel){
		this.top_ausflugsziel = top_ausflugsziel;
	}

    // getting change_index
    public int getChange_index(){
        return this.change_index;
    }
    // setting change_index
    public void setChange_index(int change_index){
        this.change_index = change_index;
    }

    // getting changed_date
    public String getChanged_date(){
        return this.changed_date;
    }
    // setting changed_date
    public void setChanged_date(String changed_date){
        this.changed_date = changed_date;
    }

	// getting visited_date
	public String getVisited_date(){
		return this._visited_date;
	}
	// setting visited_date
	public void setVisited_date(String visited_date){
		this._visited_date = visited_date;
	}

	// getting change_index
	public int getVisited_id(){
		return this._visited_id;
	}
	// setting change_index
	public void setVisited_id(int visited_id){
		this._visited_id = visited_id;
	}

    // getting favorit
    public boolean getFavorit(){
        return this._favorit;
    }
    // setting favorit
    public void setFavorit(boolean favorit){
        this._favorit = favorit;
    }

	public double getDistance()
	{
		return this._distance;
	}
	public void setDistance(double distance)
	{
		this._distance=distance;
	}

	@Override
	public LatLng getPosition() {
		try {
			return new LatLng(this.latitude, this.longitude);
		}catch(Exception e){
			return new LatLng(0, 0);
		}
	}

	@Override
	public String getTitle() {
		return getName();
	}

	@Override
	public String getSnippet() {
		return getBeschreibung();
	}


	public boolean getTodayActive(){
		return this._todayActive;
	}

	public void setTodayActive(boolean todayActive){
		this._todayActive = todayActive;
	}


	public boolean getVisited(){
		return this._visited;
	}

	public void setVisited(boolean visited){
		this._visited = visited;
	}

	public String getSearchStr(){
		return this._searchstr;
	}

	public void setSearchStr(String searchstr){
		this._searchstr = searchstr;
	}

	public String getNoecIndex(){
		return this.noecardwebsiteid;
	}

	public void setNoecIndex(String noec_index){
		this.noecardwebsiteid = noec_index;
	}

    public String getGooglePlaceId(){
        return this.googleplaceid;
    }

    public void setGooglePlaceId(String google_place_id){
        this.googleplaceid = google_place_id;
    }



}