/*
 * Copyright (C) 2016 Team OctOs
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

package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

import org.cyanogenmod.internal.logging.CMMetricsLogger;

/**
 * KernelAdiutor quick settings tile
 */
public class KernelAdiutorTile extends QSTile<QSTile.BooleanState> {

    private static final Intent KERNEL_ADIUTOR = new Intent().setComponent(new ComponentName(
            "com.grarak.kerneladiutor", "com.grarak.kerneladiutor.MainActivity"));

    public KernelAdiutorTile(Host host) {
        super(host);
    }

    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
    }

    @Override
    protected void handleClick() {
        mHost.startActivityDismissingKeyguard(KERNEL_ADIUTOR);
    }

    @Override
    protected void handleLongClick() {
    }

    private void updateState() {
    }

    @Override
    public int getMetricsCategory() {
        return CMMetricsLogger.DONT_LOG;
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        state.label = mContext.getString(R.string.quick_settings_kernel_adiutor_label);
        state.icon = ResourceIcon.get(R.drawable.ic_qs_kernel);
    }

}
