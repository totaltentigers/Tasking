package me.jakemoritz.tasking.fragment;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import me.jakemoritz.tasking.R;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(){
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setRetainInstance(true);
        return settingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences from XML resource
        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Preference aboutPreference = preferenceScreen.findPreference(getString(R.string.pref_about_key));

        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });
    }
}
