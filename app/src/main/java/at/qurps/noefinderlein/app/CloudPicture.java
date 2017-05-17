package at.qurps.noefinderlein.app;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman on 17.04.17.
 */

public class CloudPicture {

    private String locationid = null;
    private String pictureurl = null;
    private String noecidx = null;
    private String locationname = null;
    private String photoreference = null;

    public CloudPicture(){

    }
    public CloudPicture(String _locationid, String _pictureurl, String _noecidx, String _locationname, String _photoreference){
        this.locationid = _locationid;
        this.pictureurl = _pictureurl;
        this.noecidx = _noecidx;
        this.locationname = _locationname;
        this.photoreference = _photoreference;
    }
    public CloudPicture(String _photoreference, String _locationname){
        this.photoreference = _photoreference;
        this.locationname = _locationname;
    }

    public CloudPicture(String _locationid, String _pictureurl, String _noecidx, String _locationname){
        this.locationid = _locationid;
        this.pictureurl = _pictureurl;
        this.noecidx = _noecidx;
        this.locationname = _locationname;
    }


    public String getLocationname() {
        return this.locationname;
    }
    public String getPhotoreference() {
        return this.photoreference;
    }
    public String getPictureurl() {
        return this.pictureurl;
    }
    public String getLocationid() {
        return this.locationid;
    }
    public String getNoecidx() {
        return this.noecidx;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("locationid", locationid);
        result.put("pictureurl", pictureurl);
        result.put("noecidx", noecidx);
        result.put("locationname", locationname);
        result.put("photoreference", photoreference);

        return result;
    }
}
