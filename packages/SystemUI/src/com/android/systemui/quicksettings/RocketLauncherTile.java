package com.android.systemui.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class RocketLauncherTile extends QuickSettingsTile {

    private final Context mContext;

    public RocketLauncherTile(Context context, final QuickSettingsController qsc) {
        super(context, qsc);

        mContext = context;

        mLabel = mContext.getString(R.string.quick_settings_rocket_launcher);
        mDrawable = R.drawable.ic_home_rocket_holo_dark;

        mOnClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mQsc.mBar.collapseAllPanels(true);
                Intent rocketLauncher = new Intent().setClassName("com.cyanogenmod.trebuchet",
                "com.cyanogenmod.trebuchet.RocketLauncher");
                rocketLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivityAsUser(rocketLauncher, new UserHandle(UserHandle.USER_CURRENT));
            }
        };
    }
}
