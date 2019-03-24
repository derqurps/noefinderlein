package at.qurps.noefinderlein.app;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;

import java.util.List;


public class Fragment_Settings extends PreferenceFragment {

    public static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
        Preference button = findPreference(Activity_Settings.KEY_PREF_SHOW_LOG);
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                List<String> logs = HyperLog.getDeviceLogsAsStringList(false);
                String text = TextUtils.join("\n", logs);

                Bundle arguments = new Bundle();
                arguments.putString(DialogFragment_LogView.KEY_ARGUMENT_TEXT, text);



                FragmentManager fm = getActivity().getFragmentManager();
                DialogFragment_LogView newFragment = new DialogFragment_LogView();
                newFragment.setArguments(arguments);
                newFragment.show(fm, "logviewer");

                return true;
            }
        });
    }
}
