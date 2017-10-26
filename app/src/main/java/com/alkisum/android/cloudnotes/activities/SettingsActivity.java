package com.alkisum.android.cloudnotes.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.alkisum.android.cloudlib.utils.CloudPref;
import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.dialogs.ColorPaletteDialog;
import com.alkisum.android.cloudnotes.ui.AppBar;
import com.alkisum.android.cloudnotes.ui.ColorPref;
import com.alkisum.android.cloudnotes.ui.ThemePref;
import com.alkisum.android.cloudnotes.utils.Pref;

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

        ThemePref.applyTheme(this);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        // Toolbar
        Toolbar toolbar = AppBar.inflate(this, R.id.settings_stub_app_bar);
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

        /**
         * Preference for primary color.
         */
        private Preference primaryColorPref;

        /**
         * Preference for accent color.
         */
        private Preference accentColorPref;

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            // Theme
            themePref = (ListPreference) findPreference(Pref.THEME);
            themePref.setSummary(ThemePref.getSummary(getActivity()));

            // Primary color
            primaryColorPref = findPreference(Pref.PRIMARY_COLOR);
            primaryColorPref.setSummary(ColorPref.getPrimarySummary(
                    getActivity()));
            primaryColorPref.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(
                                final Preference preference) {
                            showColorPaletteDialog(
                                    ColorPaletteDialog.PRIMARY_USE);
                            return false;
                        }
                    }
            );

            // Accent color
            accentColorPref = findPreference(Pref.ACCENT_COLOR);
            accentColorPref.setSummary(ColorPref.getAccentSummary(
                    getActivity()));
            accentColorPref.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(
                                final Preference preference) {
                            showColorPaletteDialog(
                                    ColorPaletteDialog.ACCENT_USE);
                            return false;
                        }
                    }
            );

            // Do not show light status bar preference for API < 23
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                PreferenceCategory category = (PreferenceCategory)
                        findPreference("interface");
                SwitchPreference lightStatusBar = (SwitchPreference)
                        findPreference("lightStatusBar");
                category.removePreference(lightStatusBar);
            }

            // About
            Preference aboutPref = findPreference(Pref.ABOUT);
            aboutPref.setOnPreferenceClickListener(
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
                    Pref.reload(getActivity());
                    break;
                case Pref.PRIMARY_COLOR:
                    Pref.reload(getActivity());
                    break;
                case Pref.ACCENT_COLOR:
                    Pref.reload(getActivity());
                    break;
                case Pref.LIGHT_STATUS_BAR:
                    Pref.reload(getActivity());
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

        /**
         * Show the dialog to choose the color to apply to the given usage.
         *
         * @param usage Usage of the color
         */
        private void showColorPaletteDialog(final int usage) {
            ColorPaletteDialog d = ColorPaletteDialog.newInstance(usage);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            d.show(activity.getSupportFragmentManager(),
                    ColorPaletteDialog.FRAGMENT_TAG);
        }
    }
}
