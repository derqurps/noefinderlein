package at.qurps.noefinderlein.app;


import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;


import android.preference.PreferenceFragment;

public class Activity_Settings extends Activity_AppCompatPreference {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = true;

    private static final String TAG = "Activity_Settings";

    public static final String KEY_PREF_DOWNLOAD_NEW = "settings_download_new";
    public static final String KEY_PREF_OVERWRITE_YEAR = "pref_overwrite_year_man";
    public static final String KEY_PREF_OVERWRITE_YEAR_MAN = "year_chosen_2";
    public static final String KEY_PREF_OFFLINE_MODE = "pref_offline_mode";
    public static final String KEY_PREF_FILTER_VISITED = "pref_filter_visited";
    public static final String KEY_PREF_LOAD_OPEN_DATA = "pref_load_open_data";
    public static final String KEY_PREF_LOAD_PICTURES = "pref_load_pictures";
    public static final String KEY_PREF_LOAD_LOGGING = "pref_load_logging";
    public static final String KEY_PREF_SHOW_LOG = "pref_show_log";



    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Fragment_Settings())
                .commit();
    }


    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || Fragment_Settings.class.getName().equals(fragmentName);
    }



}
