/*
 * Copyright (C) 2012 Sven Dawitz for the CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class PerformanceProfileTile extends QuickSettingsTile {

    private String[] mEntries;
    private TypedArray mTypedArrayDrawables;
    private int mCurrentValue;

    private String mPerfProfileDefaultEntry;
    private String[] mPerfProfileValues;

    public PerformanceProfileTile(Context context, QuickSettingsController qsc) {
        super(context, qsc);

        Resources res = context.getResources();
        mEntries = res.getStringArray(com.android.internal.R.array.perf_profile_entries);
        mTypedArrayDrawables = res.obtainTypedArray(R.array.perf_profile_drawables);

        mPerfProfileDefaultEntry = res.getString(
                com.android.internal.R.string.config_perf_profile_default_entry);
        mPerfProfileValues = res.getStringArray(com.android.internal.R.array.perf_profile_values);

        updateCurrentValue();

        // Register a callback to detect changes in system properties
        qsc.registerObservedContent(Settings.System.getUriFor(
                Settings.System.PERFORMANCE_PROFILE), this);

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToNextProfile();
                if (isFlipTilesEnabled()) {
                    flipTile(0);
                }
            }
        };
    }

    @Override
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private void changeToNextProfile() {
        int current = mCurrentValue + 1;
        if (current >= mPerfProfileValues.length) {
            current = 0;
        }
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.PERFORMANCE_PROFILE, mPerfProfileValues[current]);
    }

    private void updateCurrentValue() {
        String perfProfile = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.PERFORMANCE_PROFILE);
        if (perfProfile == null) {
            perfProfile = mPerfProfileDefaultEntry;
        }

        int count = mPerfProfileValues.length;
        for (int i = 0; i < count; i++) {
            if (mPerfProfileValues[i].equals(perfProfile)) {
                mCurrentValue = i;
                return;
            }
        }

        // Something was wrong
        mCurrentValue = 0;
    }

    private synchronized void updateTile() {
        mDrawable = mTypedArrayDrawables.getResourceId(mCurrentValue, -1);
        mLabel = mEntries[mCurrentValue];
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        updateCurrentValue();
        updateResources();
    }

    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        updateCurrentValue();
        updateResources();
    }

}

