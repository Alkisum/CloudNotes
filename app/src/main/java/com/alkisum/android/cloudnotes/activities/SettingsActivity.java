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

import com.alkisum.android.cloudlib.utils.CloudPref;
import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.dialogs.ColorPaletteDialog;
import com.alkisum.android.cloudnotes.ui.AppBar;
import com.alkisum.android.cloudnotes.ui.ColorPref;
import com.alkisum.android.cloudnotes.ui.ThemePref;
import com.alkisum.android.cloudnotes.utils.Pref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.ButterKnife;

/**
 * Activity showing the application settings.
 *
 * @author Alkisum
 * @version 2.7
 * @since 1.1
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemePref.applyTheme(this);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        ThemePref.applyViews(this);

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
                    preference -> {
                        showColorPaletteDialog(ColorPaletteDialog.PRIMARY_USE);
                        return false;
                    }
            );

            // Accent color
            accentColorPref = findPreference(Pref.ACCENT_COLOR);
            accentColorPref.setSummary(ColorPref.getAccentSummary(
                    getActivity()));
            accentColorPref.setOnPreferenceClickListener(
                    preference -> {
                        showColorPaletteDialog(ColorPaletteDialog.ACCENT_USE);
                        return false;
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

            // Do not show light navigation bar preference for API < 26
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                PreferenceCategory category = (PreferenceCategory)
                        findPreference("interface");
                SwitchPreference lightNavigationBar = (SwitchPreference)
                        findPreference("lightNavigationBar");
                category.removePreference(lightNavigationBar);
            }

            // About
            Preference aboutPref = findPreference(Pref.ABOUT);
            aboutPref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return false;
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
                case Pref.LIGHT_NAVIGATION_BAR:
                    Pref.reload(getActivity());
                    break;
                case CloudPref.SAVE_ADDRESS:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_ADDRESS,
                            CloudPref.DEFAULT_SAVE_ADDRESS)) {
                        discardCloudInfo(CloudPref.ADDRESS, sharedPreferences);
                    }
                    break;
                case CloudPref.SAVE_PATH:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_PATH,
                            CloudPref.DEFAULT_SAVE_PATH)) {
                        discardCloudInfo(CloudPref.PATH, sharedPreferences);
                    }
                    break;
                case CloudPref.SAVE_USERNAME:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_USERNAME,
                            CloudPref.DEFAULT_SAVE_USERNAME)) {
                        discardCloudInfo(CloudPref.USERNAME, sharedPreferences);
                    }
                    break;
                case CloudPref.SAVE_PASSWORD:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_PASSWORD,
                            CloudPref.DEFAULT_SAVE_PASSWORD)) {
                        discardCloudInfo(CloudPref.PASSWORD, sharedPreferences);
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * Discard cloud connection information. Called when the user turn
         * off one of the save info settings.
         *
         * @param key        Key identifying the value to discard
         * @param sharedPref Shared Preferences
         */
        private void discardCloudInfo(final String key,
                                      final SharedPreferences sharedPref) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(key, "");
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
