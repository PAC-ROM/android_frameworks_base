/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2011 Colin McDonough
 * This code has been modified.  Portions copyright (C) AOKP by Mike Wilson (Zaphod-Beeblebrox)
 * This code has been modified.  Portions copyright (C) 2012, ParanoidAndroid Project.
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
import android.content.Intent;
import android.app.PendingIntent;

import com.android.systemui.R;

public class TorchToggle extends Toggle {

    private static final String TAG = "TorchToggle";

    public static final String KEY_TORCH_ON = "torch_on";

    private boolean mIsTorchOn;
    private Context mContext;
    PendingIntent torchIntent;

    public TorchToggle(Context context) {
        super(context);
        setLabel(R.string.toggle_torch);
        if (mToggle.isChecked()) {
            setIcon(R.drawable.toggle_torch);
        } else {
            setIcon(R.drawable.toggle_torch_off);
        }
        mContext = context;
        updateState();
    }

    @Override
    protected boolean updateInternalToggleState() {
        mToggle.setChecked(mIsTorchOn);
        if (mToggle.isChecked()) {
            setIcon(R.drawable.toggle_torch);
            return true;
        } else {
            setIcon(R.drawable.toggle_torch_off);
            return false;
        }
    }

    @Override
    protected void onCheckChanged(boolean isChecked) {
        Intent i = new Intent("net.cactii.flash2.TOGGLE_FLASHLIGHT");
        i.putExtra("bright", isChecked);
        mContext.sendBroadcast(i);
        mIsTorchOn = isChecked;
        updateState();
    }

    @Override
    protected boolean onLongPress() {
        return false;
    }
}
