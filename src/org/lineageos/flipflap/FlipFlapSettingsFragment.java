/*
 * Copyright (c) 2017 The LineageOS Project
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * Also add information on how to contact you by electronic and paper mail.
 *
 */

package org.lineageos.flipflap;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;

import org.lineageos.flipflap.R;

public class FlipFlapSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public final String TAG = "FlipFlapSettings";

    private final String KEY_DESIGN_CATEGORY = "category_design";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.flipflapsettings_panel);

        setupTimeoutPreference(FlipFlapUtils.KEY_TIMEOUT_PLUGGED);
        setupTimeoutPreference(FlipFlapUtils.KEY_TIMEOUT_UNPLUGGED);

        int cover = FlipFlapUtils.getCoverStyle(getActivity());
        if (!FlipFlapUtils.showsChargingStatus(cover)) {
            PreferenceScreen ps = getPreferenceScreen();
            ps.removePreference(ps.findPreference(KEY_DESIGN_CATEGORY));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = (String) newValue;
        String key = preference.getKey();
        Log.d(TAG, "Preference changed: " + key + ": " + value);

        switch (key) {
            case FlipFlapUtils.KEY_TIMEOUT_PLUGGED:
            case FlipFlapUtils.KEY_TIMEOUT_UNPLUGGED:
                setTimeoutSummary(preference, Integer.parseInt(value));
                return true;

            default:
                return true;

        }
    }

    private void setupTimeoutPreference(String key) {
        ListPreference list = (ListPreference) findPreference(key);
        list.setOnPreferenceChangeListener(this);
        setTimeoutSummary(list, FlipFlapUtils.getTimeout(getActivity(), key));
    }

    private void setTimeoutSummary(Preference pref, int timeOut) {
        pref.setSummary(timeOut < 0
                ? R.string.timeout_summary_never
                : timeOut == 0
                    ? R.string.timeout_summary_immediately
                    : R.string.timeout_summary);
    }
}
