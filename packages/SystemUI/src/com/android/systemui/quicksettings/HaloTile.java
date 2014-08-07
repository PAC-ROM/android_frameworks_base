package com.android.systemui.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.android.internal.view.RotationPolicy;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;

public class HaloTile extends QuickSettingsTile {

    public HaloTile(Context context, QuickSettingsController qsc, Handler handler) {
        super(context, qsc);

        mOnClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.PAC.putInt(mContext.getContentResolver(), Settings.PAC.HALO_ACTIVE, getHaloState() ? 1 : 0);
            }
        };

        mOnLongClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //startSettingsActivity(Settings.HaloActivity);
                return true;
            }
        };
        qsc.registerObservedContent(Settings.PAC.getUriFor(Settings.PAC.HALO_ACTIVE)
                , this);
    }

    @Override
    public void updateResources() {
        updateTile();
        updateQuickSettings();
    }

    private synchronized void updateTile() {
        if(!getHaloState()){
            mDrawable = R.drawable.ic_qs_halo_on;
            mLabel = mContext.getString(R.string.halo_on);
        }else{
            mDrawable = R.drawable.ic_qs_halo_off;
            mLabel = mContext.getString(R.string.halo_off);
        }
    }

    @Override
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    private boolean getHaloState() {
        return !Settings.PAC.getBoolean(mContext.getContentResolver(), Settings.PAC.HALO_ACTIVE, false);
    }

    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        updateResources();
    }
}
