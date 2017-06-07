package me.jakemoritz.tasking.fragment;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import me.jakemoritz.tasking.R;

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences from XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
