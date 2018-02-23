package at.qurps.noefinderlein.app;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman on 17.04.17.
 */

public class CloudPicture implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.locationid);
        dest.writeString(this.pictureurl);
        dest.writeString(this.noecidx);
        dest.writeString(this.locationname);
        dest.writeString(this.photoreference);
    }

    protected CloudPicture(Parcel in) {
        this.locationid = in.readString();
        this.pictureurl = in.readString();
        this.noecidx = in.readString();
        this.locationname = in.readString();
        this.photoreference = in.readString();
    }

    public static final Parcelable.Creator<CloudPicture> CREATOR = new Parcelable.Creator<CloudPicture>() {
        @Override
        public CloudPicture createFromParcel(Parcel source) {
            return new CloudPicture(source);
        }

        @Override
        public CloudPicture[] newArray(int size) {
            return new CloudPicture[size];
        }
    };
}
