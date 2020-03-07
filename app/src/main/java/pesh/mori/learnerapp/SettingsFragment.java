package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Created by Nick Otto on 01/07/2019.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private Preference mPreference;

    public SettingsFragment(){

    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences,s);

        mPreference = (Preference)findPreference("profile");
        mPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(),ProfileActivity.class));
                return true;
            }
        });
    }
}
