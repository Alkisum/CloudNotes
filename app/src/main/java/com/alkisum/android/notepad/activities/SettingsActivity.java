package com.alkisum.android.notepad.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.alkisum.android.cloudops.utils.CloudPref;
import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.utils.Pref;
import com.alkisum.android.notepad.utils.Theme;

import butterknife.ButterKnife;

/**
 * Activity showing the application settings.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Theme.setCurrentTheme(this);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(
                R.id.settings_frame_content, new SettingsFragment()).commit();
    }

    /**
     * SettingsFragment extending PreferenceFragment.
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        /**
         * ListPreference for themes.
         */
        private ListPreference themePref;

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            // Theme
            themePref = (ListPreference) findPreference(Pref.THEME);
            themePref.setSummary(Theme.getSummary(getActivity()));

            // About
            Preference aboutPreference = findPreference(Pref.ABOUT);
            aboutPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(
                                final Preference preference) {
                            startActivity(new Intent(getActivity(),
                                    AboutActivity.class));
                            return false;
                        }
                    });
        }

        @Override
        public final void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public final void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public final void onSharedPreferenceChanged(
                final SharedPreferences sharedPreferences, final String key) {
            switch (key) {
                case Pref.THEME:
                    themePref.setSummary(Theme.getSummary(getActivity()));
                    Theme.reload(getActivity());
                    break;
                case CloudPref.SAVE_OWNCLOUD_INFO:
                    if (!sharedPreferences.getBoolean(
                            CloudPref.SAVE_OWNCLOUD_INFO, false)) {
                        discardOwnCloudInfo(sharedPreferences);
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * Discard ownCloud connection information. Called when the user turn
         * off the save ownCloud info settings.
         *
         * @param sharedPref Shared Preferences
         */
        private void discardOwnCloudInfo(final SharedPreferences sharedPref) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(CloudPref.ADDRESS, "");
            editor.putString(CloudPref.PATH, "");
            editor.putString(CloudPref.USERNAME, "");
            editor.apply();
        }
    }
}
