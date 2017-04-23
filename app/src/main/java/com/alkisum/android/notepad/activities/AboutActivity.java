package com.alkisum.android.notepad.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.alkisum.android.notepad.BuildConfig;
import com.alkisum.android.notepad.R;
import com.alkisum.android.notepad.utils.Pref;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Activity listing information about the application.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.about_toolbar);
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

        /**
         * Format for build date.
         */
        public static final SimpleDateFormat DATE_BUILD =
                new SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault());

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.about_preferences);

            // Build version
            Preference versionPreference = findPreference(Pref.BUILD_VERSION);
            versionPreference.setSummary(BuildConfig.VERSION_NAME);

            // Build date
            Preference datePreference = findPreference(Pref.BUILD_DATE);
            datePreference.setSummary(DATE_BUILD.format(
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
