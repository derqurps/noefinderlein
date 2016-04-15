package at.qurps.noefinderlein.app;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Location_NoeC implements ClusterItem {

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
	public static final String KEY_FAVORIT = "favorit";

	//private variables
    int _id;
	int _nummer;
    int _jahr;
	int _kat;
	int _reg;
	String _name;
	String _email;
	double _latitude;
	double _longitude;
	String _adr_plz;
	String _tel;
	String _fax;
	String _anreise;
	String _geoeffnet;
	String _adr_ort;
	String _adr_street;
	String _tipp;
	boolean _rollstuhl;
	boolean _kinderwagen;
	boolean _hund;
	boolean _gruppe;
	String _webseite;
	String _beschreibung;
	String _aussersonder;
	String _eintritt;
	String _ersparnis;
	boolean _top_ausflugsziel;
    int _change_index;
    String _changed_date;
    boolean _favorit;


	double _distance;
	String _visited_date;
	int _visited_id;
	
	
	// Empty constructor
	public Location_NoeC(){

	}
	// constructor
	public Location_NoeC(int id,
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
        this._id= id;
		this._nummer = nummer;
        this._jahr = jahr;
		this._kat=kat;
		this._reg=reg;
		this._name=name;
		this._email=email;
		this._latitude=latitude;
		this._longitude=longitude;
		this._adr_plz=adr_plz;
		this._tel=tel;
		this._fax=fax;
		this._anreise=anreise;
		this._geoeffnet=geoeffnet;
		this._adr_ort=adr_ort;
		this._adr_street=adr_street;
		this._tipp=tipp;
		this._rollstuhl=rollstuhl;
		this._kinderwagen=kinderwagen;
		this._hund=hund;
		this._gruppe=gruppe;
		this._webseite=webseite;
		this._beschreibung=beschreibung;
		this._aussersonder=aussersonder;
		this._eintritt=eintritt;
		this._ersparnis=ersparnis;
		this._top_ausflugsziel=top_ausflugsziel;
        this._change_index=change_index;
        this._changed_date=changed_date;
        this._favorit=favorit;
		
	}

	// constructor
	public Location_NoeC(int id,
						 int nummer,
						 int jahr,
						 int kat,
						 int reg,
						 String name){
		this._id= id;
		this._nummer = nummer;
        this._jahr = jahr;
		this._kat=kat;
		this._reg=reg;
		this._name=name;
	}
    // getting ID
    public int getId(){
        return this._id;
    }
    // setting ID
    public void setId(int id){
        this._id = id;
    }

    // getting Nummer
	public int getNummer(){
	    return this._nummer;
	}
	// setting Nummer
	public void setNummer(int nummer){
	    this._nummer = nummer;
	}

    // getting Jahr
    public int getJahr(){
        return this._jahr;
    }
    // setting Nummer
    public void setJahr(int jahr){
        this._jahr = jahr;
    }

	
	// getting kat
	public int getKat(){
		return this._kat;
	}
	// setting kat
	public void setKat(int kat){
		this._kat = kat;
	}
	// getting reg
	public int getReg(){
		return this._reg;
	}
	// setting reg
	public void setReg(int reg){
		this._reg = reg;
	}
	// getting name
	public String getName(){
		return this._name;
	}
	// setting name
	public void setName(String name){
		this._name = name;
	}
	// getting email
	public String getEmail(){
		return this._email;
	}
	// setting email
	public void setEmail(String email){
		this._email = email;
	}
	// getting latitude
	public double getLatitude(){
		return this._latitude;
	}
	// setting latitude
	public void setLatitude(double latitude){
		this._latitude = latitude;
	}
	// getting longitude
	public double getLongitude(){
		return this._longitude;
	}
	// setting longitude
	public void setLongitude(double longitude){
		this._longitude = longitude;
	}
	// getting adr_plz
	public String getAdr_plz(){
		return this._adr_plz;
	}
	// setting adr_plz
	public void setAdr_plz(String adr_plz){
		this._adr_plz = adr_plz;
	}
	// getting tel
	public String getTel(){
		return this._tel;
	}
	// setting tel
	public void setTel(String tel){
		this._tel = tel;
	}
	// getting fax
	public String getFax(){
		return this._fax;
	}
	// setting fax
	public void setFax(String fax){
		this._fax = fax;
	}
	// getting anreise
	public String getAnreise(){
		return this._anreise;
	}
	// setting anreise
	public void setAnreise(String anreise){
		this._anreise = anreise;
	}
	// getting geoeffnet
	public String getGeoeffnet(){
		return this._geoeffnet;
	}
	// setting geoeffnet
	public void setGeoeffnet(String geoeffnet){
		this._geoeffnet = geoeffnet;
	}
	// getting adr_ort
	public String getAdr_ort(){
		return this._adr_ort;
	}
	// setting adr_ort
	public void setAdr_ort(String adr_ort){
		this._adr_ort = adr_ort;
	}
	// getting adr_street
	public String getAdr_street(){
		return this._adr_street;
	}
	// setting adr_street
	public void setAdr_street(String adr_street){
		this._adr_street = adr_street;
	}
	// getting tipp
	public String getTipp(){
		return this._tipp;
	}
	// setting tipp
	public void setTipp(String tipp){
		this._tipp = tipp;
	}
	// getting rollstuhl
	public boolean getRollstuhl(){
		return this._rollstuhl;
	}
	// setting rollstuhl
	public void setRollstuhl(boolean rollstuhl){
		this._rollstuhl = rollstuhl;
	}
	// getting kinderwagen
	public boolean getKinderwagen(){
		return this._kinderwagen;
	}
	// setting kinderwagen
	public void setKinderwagen(boolean kinderwagen){
		this._kinderwagen = kinderwagen;
	}
	// getting hund
	public boolean getHund(){
		return this._hund;
	}
	// setting hund
	public void setHund(boolean hund){
		this._hund = hund;
	}
	// getting gruppe
	public boolean getGruppe(){
		return this._gruppe;
	}
	// setting gruppe
	public void setGruppe(boolean gruppe){
		this._gruppe = gruppe;
	}

	// getting webseite
	public String getWebseite(){
		return this._webseite;
	}
	// setting webseite
	public void setWebseite(String webseite){
		this._webseite = webseite;
	}

	// getting beschreibung
	public String getBeschreibung(){
		return this._beschreibung;
	}
	// setting beschreibung
	public void setBeschreibung(String beschreibung){
		this._beschreibung = beschreibung;
	}
	// getting aussersonder
	public String getAussersonder(){
		return this._aussersonder;
	}
	// setting aussersonder
	public void setAussersonder(String aussersonder){
		this._aussersonder = aussersonder;
	}
	// getting eintritt
	public String getEintritt(){
		return this._eintritt;
	}
	// setting eintritt
	public void setEintritt(String eintritt){
		this._eintritt = eintritt;
	}
	// getting ersparnis
	public String getErsparnis(){
		return this._ersparnis;
	}
	// setting ersparnis
	public void setErsparnis(String ersparnis){
		this._ersparnis = ersparnis;
	}
	// getting top_ausflugsziel
	public boolean getTop_ausflugsziel(){
		return this._top_ausflugsziel;
	}
	// setting top_ausflugsziel
	public void setTop_ausflugsziel(boolean top_ausflugsziel){
		this._top_ausflugsziel = top_ausflugsziel;
	}

    // getting change_index
    public int getChange_index(){
        return this._change_index;
    }
    // setting change_index
    public void setChange_index(int change_index){
        this._change_index = change_index;
    }

    // getting changed_date
    public String getChanged_date(){
        return this._changed_date;
    }
    // setting changed_date
    public void setChanged_date(String changed_date){
        this._changed_date = changed_date;
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
			return new LatLng(_latitude, _longitude);
		}catch(Exception e){
			return new LatLng(0, 0);
		}
	}
}