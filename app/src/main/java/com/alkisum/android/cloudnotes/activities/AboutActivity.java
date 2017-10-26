package com.alkisum.android.cloudnotes.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.alkisum.android.cloudnotes.BuildConfig;
import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.ui.AppBar;
import com.alkisum.android.cloudnotes.ui.ThemePref;
import com.alkisum.android.cloudnotes.utils.Format;
import com.alkisum.android.cloudnotes.utils.Pref;

import java.util.Date;

import butterknife.ButterKnife;

/**
 * Activity listing information about the application.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemePref.applyTheme(this);

        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        // Toolbar
        Toolbar toolbar = AppBar.inflate(this, R.id.about_stub_app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(
                R.id.about_frame_content, new AboutFragment()).commit();
    }

    /**
     * AboutFragment extending PreferenceFragment.
     */
    public static class AboutFragment extends PreferenceFragment {

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.about_preferences);

            // Build version
            Preference versionPreference = findPreference(Pref.BUILD_VERSION);
            versionPreference.setSummary(BuildConfig.VERSION_NAME);

            // Build date
            Preference datePreference = findPreference(Pref.BUILD_DATE);
            datePreference.setSummary(Format.DATE_BUILD.format(
                    new Date(BuildConfig.TIMESTAMP)));

            // Github
            Preference githubPreference = findPreference(Pref.GITHUB);
            githubPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(
                                final Preference preference) {
                            Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(getString(R.string.about_github))
                            );
                            startActivity(intent);
                            return false;
                        }
                    });
        }
    }
}
