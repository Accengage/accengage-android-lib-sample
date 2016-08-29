package com.accengage.samples.backstack;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.accengage.samples.R;

public class BackstackPreferencesActivity extends Activity {

    public static final String BACKSTACK_PREFERENCES_FILE_NAME = "com.accengage.samples.backstack.preferences";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display backstack preferences fragment
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new BackstackPreferencesFragment())
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BackstackPreferencesFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //PreferenceManager.setDefaultValues(getActivity(), R.xml.geofence_dbview_settings, false);
            getPreferenceManager().setSharedPreferencesName(BACKSTACK_PREFERENCES_FILE_NAME);

            addPreferencesFromResource(R.xml.pref_backstack);
        }
    }
}
