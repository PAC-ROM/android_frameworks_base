/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.toggles;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.provider.Settings;
import android.os.Handler;

import com.android.systemui.R;

public class TickerToggle extends Toggle {

    Context mContext;

    boolean mTickerPopUpEnabled;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.STATUS_BAR_NOTIFICATION_POPUP), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            mTickerPopUpEnabled = isTickerPopUpEnabled();
            updateState();
        }
    }

    public TickerToggle(Context context) {
        super(context);
        mContext = context;
        setLabel(R.string.toggle_ticker);
        mTickerPopUpEnabled = isTickerPopUpEnabled();

        SettingsObserver obs = new SettingsObserver(new Handler());
        obs.observe();

        updateState();
    }

    private boolean isTickerPopUpEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
            Settings.System.STATUS_BAR_NOTIFICATION_POPUP, 1) == 1;
    }

    @Override
    protected void onCheckChanged(boolean isChecked) {
        Settings.System.putInt(mContext.getContentResolver(),
            Settings.System.STATUS_BAR_NOTIFICATION_POPUP, isChecked ? 1 : 0);
        updateState();
    }

    @Override
    protected boolean onLongPress() {
        return true;
    }

    @Override
    protected boolean updateInternalToggleState() {
        mToggle.setChecked(mTickerPopUpEnabled);
        if (mToggle.isChecked()) {
            setIcon(R.drawable.toggle_ticker);
        } else {
            setIcon(R.drawable.toggle_ticker_off);
        }
        return mToggle.isChecked();
    }
}
