package me.jakemoritz.tasking_new.fragment;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.View;

import me.jakemoritz.tasking_new.R;
import me.jakemoritz.tasking_new.activity.MainActivity;

public class SettingsFragment extends PreferenceFragment {

    private MainActivity mainActivity;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
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
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_main, AboutFragment.newInstance(mainActivity))
                        .addToBackStack(AboutFragment.class.getSimpleName())
                        .commit();

                return true;
            }
        });

        Preference privacyPolicyPreference = preferenceScreen.findPreference(getString(R.string.pref_privacy_policy_key));
        privacyPolicyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://www.iubenda.com/privacy-policy/8158356");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;

            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.enableUpNavigation(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mainActivity = (MainActivity) context;
    }
}
