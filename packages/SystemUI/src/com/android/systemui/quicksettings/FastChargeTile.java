package com.android.systemui.quicksettings;

import android.content.Context;
import android.view.View;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FastChargeTile extends QuickSettingsTile {

    private final String TAG = FastChargeTile.class.getSimpleName();
    private final String FFC_PATH = "/sys/kernel/fast_charge/force_fast_charge";

    public FastChargeTile(Context context, QuickSettingsController qsc) {
        super(context, qsc);

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableFastcharge();
                updateResources();
                if (isFlipTilesEnabled()) flipTile(0);
            }
        };
    }

    @Override
    void onPostCreate() {
        updateResources();
        super.onPostCreate();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        boolean enable = isFastchargeOn();
        mDrawable = enable ? R.drawable.ic_qs_fcharge_on : R.drawable.ic_qs_fcharge_off;
        mLabel = mContext.getString(enable ? R.string.quick_settings_fcharge_on_label :
            R.string.quick_settings_fcharge_off_label);
    }

    private boolean isFastchargeOn() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(FFC_PATH),
                256);
            return reader.readLine().equals("1");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "cannot find " + FFC_PATH);
        } catch (IOException e) {
            Log.e(TAG, "unable to read " + FFC_PATH);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void enableFastcharge() {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(FFC_PATH, true);
            FileWriter fWriter;
            try {
                fWriter = new FileWriter(fos.getFD());
                fWriter.write(isFastchargeOn() ? "0" : "1");
                fWriter.close();
            } catch (Exception e) {
                Log.e(TAG, "unable to write " + FFC_PATH);
            } finally {
                fos.getFD().sync();
                fos.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "unable to write " + FFC_PATH);
        }
    }

}
