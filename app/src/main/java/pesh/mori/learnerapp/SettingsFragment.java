package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

/**
 * Created by Nick Otto on 01/07/2019.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private Preference mPreference;
    private SwitchPreferenceCompat mNightModePreference, mSignatureModePreference;

    public SettingsFragment(){}

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences,s);

        mPreference = (Preference)findPreference(getString(R.string.pref_profile_key));
        mPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(),ProfileActivity.class));
                return true;
            }
        });

        mNightModePreference = (SwitchPreferenceCompat)findPreference(getString(R.string.pref_night_theme_key));
        mSignatureModePreference = (SwitchPreferenceCompat)findPreference(getString(R.string.pref_sign_theme_key));
        if (new SharedPreferencesHandler(getActivity()).getNightMode()){
            mNightModePreference.setChecked(true);
            mSignatureModePreference.setChecked(false);
        }
        if (new SharedPreferencesHandler(getActivity()).getSignatureMode()){
            mSignatureModePreference.setChecked(true);
            mNightModePreference.setChecked(false);
        }

        mNightModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!new SharedPreferencesHandler(getActivity()).getNightMode()){
                    new SharedPreferencesHandler(getActivity()).setNightMode(true);
                    new SharedPreferencesHandler(getActivity()).setSignatureMode(false);
                    reload();
                } else {
                    new SharedPreferencesHandler(getActivity()).setNightMode(false);
                    reload();
                }
                return true;
            }
        });

        mSignatureModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!new SharedPreferencesHandler(getActivity()).getSignatureMode()){
                    new SharedPreferencesHandler(getActivity()).setSignatureMode(true);
                    new SharedPreferencesHandler(getActivity()).setNightMode(false);
                    reload();
                } else {
                    new SharedPreferencesHandler(getActivity()).setSignatureMode(false);
                    reload();
                }
                return true;
            }
        });
    }

    public void reload(){
        startActivity(new Intent(getActivity(),SettingsActivity.class));
        getActivity().finish();
    }
}
