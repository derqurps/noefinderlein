package at.qurps.noefinderlein.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {


	private static final String TAG = "Util";


	public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
		int scaleHeight = (int) (bm.getHeight() * scalingFactor);
		int scaleWidth = (int) (bm.getWidth() * scalingFactor);

		return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
	}

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}


	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}


	public static File getAlbumStorageDir(Context context, String albumName) {
		// Get the directory for the app's private pictures directory. 
		File file = new File(context.getExternalFilesDir(
				Environment.DIRECTORY_PICTURES), albumName);
		if (!file.mkdirs()) {
			Log.e(TAG, "Directory not created");
		}
		return file;
	}
	public static String getmkmdistance(Double distance) {
		long distance_int=Math.round(distance);
		if (distance_int>999)
		{
			return String.format("%.2f", (float)distance_int/1000) + " km";
		}
		else
		{
			return String.valueOf(distance_int) + " m";
		}
	}
    public static double distance_between(Location l1, Location l2)
    {
        //float results[] = new float[1];
    /* Doesn't work. returns inconsistent results
    Location.distanceBetween(
            l1.getLatitude(),
            l1.getLongitude(),
            l2.getLatitude(),
            l2.getLongitude(),
            results);
            */
        double lat1=l1.getLatitude();
        double lon1=l1.getLongitude();
        double lat2=l2.getLatitude();
        double lon2=l2.getLongitude();
        double R = 6371; // km
        double dLat = (lat2-lat1)*Math.PI/180;
        double dLon = (lon2-lon1)*Math.PI/180;
        lat1 = lat1*Math.PI/180;
        lat2 = lat2*Math.PI/180;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c * 1000;

        /*log_write("dist betn "+
                        d + " " +
                        l1.getLatitude()+ " " +
                        l1.getLongitude() + " " +
                        l2.getLatitude() + " " +
                        l2.getLongitude()
        );*/

        return d;
    }
	public static void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size=1024;
		try
		{
			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){}
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		/*if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}*/
		final int max_size;
		if (reqWidth>reqHeight)
		{
			max_size=reqWidth;
		}
		else
		{
			max_size=reqHeight;
		}
		if (height > max_size || width > max_size) {
			inSampleSize = (int)Math.pow(2, (int) Math.round(Math.log(max_size / 
	           (double) Math.max(height, width)) / Math.log(0.5)));
	    }

		return inSampleSize;
	}
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		options.inPurgeable=true;
		options.inInputShareable=true;
		//Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, resId), reqWidth, reqHeight, filter)
		return BitmapFactory.decodeResource(res, resId, options);
	}
    public static Bitmap resizePicFromPath(String mCurrentPhotoPath,int targetW,int targetH){
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }
    public static void setPicFromPath(ImageView mImageView,String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        Bitmap bitmap = resizePicFromPath(mCurrentPhotoPath,targetW,targetH);

        mImageView.setImageBitmap(bitmap);
    }
    // Convert the image URI to the direct file system path of the image file
    public static String GetRealPathFromURI(final Context context, final Uri ac_Uri )
    {
        String result = "";
        boolean isok = false;

        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(ac_Uri,  proj, null, null, null);
            //int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndex(proj[0]);
            result = cursor.getString(column_index);
            isok = true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isok ? result : "";
    }
    private Bitmap getBitmapFromUri (Uri uri, Context mContext) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                mContext.getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

        parcelFileDescriptor.close();
        return image;
    }
	public static int dpToPx(int dp,DisplayMetrics displayMetrics) {
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	public static int pxToDp(int px,DisplayMetrics displayMetrics) {
	    int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	    return dp;
	}
    public  static class dataholder implements Parcelable{
        public String _text;
        public String _column;
        public String _memail;
        public String _scope;
        public int _jahr;
        public int _id;
        public int _nummer;
        public String _action;
        public String _token;
        public String _photoPath;
        public String _absolutPath;
        public String _filename;

        public dataholder(){
            this._action="";
            this._memail=null;
        }
        public dataholder(String text,String column){
            this._text=text;
            this._column=column;
        }
        public dataholder(String text,String column, int aktnummer,int aktjahr){
            this._text=text;
            this._column=column;
            this._nummer=aktnummer;
            this._jahr=aktjahr;
        }
        public String gettext(){
            return _text;
        }
        public void settext(String text){
            this._text=text;
        }
        public String getcolumn(){
            return _column;
        }
        public void setcolumn(String column){
            this._column=column;
        }

        @Override
        public int describeContents() {
            return 0;
        }


        public String getContent() {
            return "Text: "+this._text+" Column: "+this._column + " Email: "+this._memail+" Scope: "+this._scope+" jahr: "+String.valueOf(this._jahr) +" id: "+String.valueOf(this._id) +" nummer: "+String.valueOf(this._nummer) +
                    " Action: "+this._action + " Token: "+this._token;
        }
        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(_text);
            parcel.writeString(_column);
            parcel.writeString(_memail);
            parcel.writeString(_scope);
            parcel.writeInt(_jahr);
            parcel.writeInt(_id);
            parcel.writeInt(_nummer);
            parcel.writeString(_action);
            parcel.writeString(_token);
            parcel.writeString(_photoPath);
            parcel.writeString(_absolutPath);
            parcel.writeString(_filename);
        }

        public static final Creator<dataholder> CREATOR = new Creator<dataholder>() {

            public dataholder createFromParcel(Parcel pc) {

            return new dataholder(pc);

            }

            public dataholder[] newArray(int size) {

            return new dataholder[size];

            }

        };

        public dataholder(Parcel pc){
            _text=pc.readString();
            _column=pc.readString();
            _memail=pc.readString();
            _scope=pc.readString();
            _jahr=pc.readInt();
            _id=pc.readInt();
            _nummer=pc.readInt();
            _action=pc.readString();
            _token=pc.readString();
            _photoPath=pc.readString();
            _absolutPath=pc.readString();
            _filename=pc.readString();
        }
    }
    public static int[] getIntArrayFromString(String sarray){
        String[] catSArray;
        int[] catIArray;
        if(sarray.toLowerCase().contains(",".toLowerCase())){
            catSArray = sarray.split(",");

        }else{
            catSArray = new String[1];
            catSArray[0] = sarray;
        }
        catIArray = new int[catSArray.length];
        for(int i=0;i<catSArray.length;i++){
            catIArray[i]=Integer.parseInt(catSArray[i]);
        }
        return catIArray;
    }
    public static void setToast(final Context mcontext, final String message,final int length) {
        ((Activity)mcontext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (length == 1) {
                    Toast.makeText(mcontext, message,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mcontext, message,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public static void colorMenuItems (Context mContext, Menu menu, int rId, int cId){
        Drawable drawable = menu.findItem(rId).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(mContext, cId));
        menu.findItem(rId).setIcon(drawable);
    }
    public final static void setDatePreferences(Context mContext, int year, int month, int day) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(DialogFragment_ChooseDates.YEAR, year);
        editor.putInt(DialogFragment_ChooseDates.MONTH, month);
        editor.putInt(DialogFragment_ChooseDates.DAY, day);

        editor.apply();
    }
    public final static void setPreferencesBoolean(Context mContext, String key, boolean setVar) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor mEditor =preferences.edit();
        mEditor.putBoolean(key, setVar);
        mEditor.apply();
    }
    public final static boolean getPreferencesBoolean(Context mContext, String key, boolean defaultVar) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(key, defaultVar);
    }
    public final static int getPreferencesInt(Context mContext, String key, int defaultVar) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getInt(key, defaultVar);
    }
    public final static int getDatePreferencesYear(Context mContext) {
        return getPreferencesInt(mContext, DialogFragment_ChooseDates.YEAR, 0);
    }
    public final static int getDatePreferencesMonth(Context mContext) {
        return getPreferencesInt(mContext, DialogFragment_ChooseDates.MONTH, 0);
    }
    public final static int getDatePreferencesDay(Context mContext) {
        return getPreferencesInt(mContext, DialogFragment_ChooseDates.DAY, 0);
    }
    private static Calendar getChosenDayAsCalendar(Context mContext){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Calendar cNow = Calendar.getInstance();

        int year = preferences.getInt(DialogFragment_ChooseDates.YEAR, 0);
        int month = preferences.getInt(DialogFragment_ChooseDates.MONTH, 0);
        int day = preferences.getInt(DialogFragment_ChooseDates.DAY, 0);
        if(year == 0 || day == 0) {
            year = cNow.get(Calendar.YEAR);
            month = cNow.get(Calendar.MONTH);
            day = cNow.get(Calendar.DAY_OF_MONTH);
        } else {
            Calendar compare = Calendar.getInstance();
            compare.set(year, month, day);
            if(compare.before(cNow)) {
                year = cNow.get(Calendar.YEAR);
                month = cNow.get(Calendar.MONTH);
                day = cNow.get(Calendar.DAY_OF_MONTH);
            }
        }
        cNow.set(year, month, day);
        return cNow;
    }
    public final static String getDBDateString(Context mContext) {

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        return format1.format(getChosenDayAsCalendar(mContext).getTime());
    }
    public final static String getDisplayDateString(Context mContext) {
        SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy");
        return format1.format(getChosenDayAsCalendar(mContext).getTime());
    }
    public final static boolean isTodaySet(Context mContext) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Calendar cNow = Calendar.getInstance();

        int year = preferences.getInt(DialogFragment_ChooseDates.YEAR, 0);
        int month = preferences.getInt(DialogFragment_ChooseDates.MONTH, 0);
        int day = preferences.getInt(DialogFragment_ChooseDates.DAY, 0);
        return ((year == (int)cNow.get(Calendar.YEAR)) && (month == (int)cNow.get(Calendar.MONTH)) && (day == (int)cNow.get(Calendar.DAY_OF_MONTH)));
    }
}
