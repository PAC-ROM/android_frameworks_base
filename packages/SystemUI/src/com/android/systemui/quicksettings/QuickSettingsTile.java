package com.android.systemui.quicksettings;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.Animator.AnimatorListener;
import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.android.systemui.R;
import java.util.Random;

import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsTileView;

public class QuickSettingsTile implements OnClickListener {

    protected final Context mContext;
    protected final ViewGroup mContainerView;
    protected final LayoutInflater mInflater;
    protected QuickSettingsTileView mTile;
    protected OnClickListener mOnClick;
    protected OnLongClickListener mOnLongClick;
    protected int mTileLayout;
    protected int mDrawable;
    protected String mLabel;
    protected BaseStatusBar mStatusbarService;
    protected QuickSettingsController mQsc;
    protected int mTileTextSize; 
    protected int mTileTextColor;

    private static final int DEFAULT_QUICK_TILES_BG_COLOR = 0xff161616;
    private static final int DEFAULT_QUICK_TILES_BG_PRESSED_COLOR = 0xff212121;

    private Handler mHandler = new Handler();

    private static int[] RandomColors = new int[] {
        android.R.color.holo_blue_dark,
        android.R.color.holo_red_dark,
        android.R.color.holo_green_dark,
        android.R.color.holo_orange_dark,
        android.R.color.holo_purple,
        android.R.color.holo_blue_bright,
        android.R.color.holo_green_light
    };

    public QuickSettingsTile(Context context, LayoutInflater inflater, QuickSettingsContainerView container, QuickSettingsController qsc) {
        mContext = context;
        mContainerView = container;
        mInflater = inflater;
        mDrawable = R.drawable.ic_notifications;
        mLabel = mContext.getString(R.string.quick_settings_label_enabled);
        mStatusbarService = qsc.mStatusBarService;
        mQsc = qsc;
        mTileLayout = R.layout.quick_settings_tile_generic;
        mTileTextSize = ((QuickSettingsContainerView) mContainerView).updateTileTextSize(); 
        mTileTextColor = ((QuickSettingsContainerView) mContainerView).updateTileTextColor(); 
    }

    public void setupQuickSettingsTile() {
            createQuickSettings();
            onPostCreate();
            updateQuickSettings();
            mTile.setOnClickListener(this);
            mTile.setOnLongClickListener(mOnLongClick);
    }

    void createQuickSettings() {
        mTile = (QuickSettingsTileView) mInflater.inflate(R.layout.quick_settings_tile, mContainerView, false);
        mTile.setContent(mTileLayout, mInflater);
        mContainerView.addView(mTile);
        setColor();
        setRandomColor();
    }

    void onPostCreate(){}

    public void onReceive(Context context, Intent intent) {}

    public void onChangeUri(ContentResolver resolver, Uri uri) {}

    public void updateResources() {
        if(mTile != null) {
            updateQuickSettings();
        }
    }

    void updateQuickSettings() {
        TextView tv = (TextView) mTile.findViewById(R.id.text);
        if (tv != null) {
            ImageView iv = (ImageView) mTile.findViewById(R.id.image);
            tv.setText(mLabel);
            iv.setImageDrawable(mContext.getResources().getDrawable(mDrawable));
            tv.setTextSize(1, mTileTextSize);
            if (mTileTextColor != -2) {
                tv.setTextColor(mTileTextColor);
            }
        }
    }

    public boolean isFlipTilesEnabled() {
        return (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES_FLIP, 1) == 1);
    }

    public boolean isRandom() {
        return (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_BG_COLOR_RANDOM, 0) == 1);
    }

    public void flipTile(int delay){
        final AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(
                mContext, R.anim.flip_right);
        anim.setTarget(mTile);
        anim.setDuration(200);
        anim.addListener(new AnimatorListener(){

            @Override
            public void onAnimationEnd(Animator animation) {
                setRandomColor();
            }
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        Runnable doAnimation = new Runnable(){
            @Override
            public void run() {
                anim.start();
            }
        };

        mHandler.postDelayed(doAnimation, delay);
    }

    void startSettingsActivity(String action) {
        Intent intent = new Intent(action);
        startSettingsActivity(intent);
    }

    void startSettingsActivity(Intent intent) {
        startSettingsActivity(intent, true);
    }

    private void startSettingsActivity(Intent intent, boolean onlyProvisioned) {
        if (onlyProvisioned && !mStatusbarService.isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ContentResolver resolver = mContext.getContentResolver();
        boolean floatingWindow = Settings.System.getBoolean(resolver, Settings.System.QS_FLOATING_WINDOW, false) == true;
        if (floatingWindow) {
            intent.addFlags(Intent.FLAG_FLOATING_WINDOW);
        }
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        mStatusbarService.collapse();
    }

    @Override
    public void onClick(View v) {
        if (mOnClick != null) {
            mOnClick.onClick(v);
        }

        ContentResolver resolver = mContext.getContentResolver();
        boolean shouldCollapse = Settings.System.getInt(resolver, Settings.System.QS_COLLAPSE_PANEL, 0) == 1;
        if (shouldCollapse || this instanceof DesktopModeTile || this instanceof HybridTile) {
            mQsc.mBar.collapseAllPanels(true);
        }
        if (isRandom()) {
            setRandomColor();
        }
    }

    private void setColor() {

        int bgColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_BG_COLOR, -2);
        int presColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_BG_PRESSED_COLOR, -2);

        if (bgColor != -2 || presColor != -2) {
            if (bgColor == -2 && !isRandom()) {
                bgColor = DEFAULT_QUICK_TILES_BG_COLOR;
            }
            if (presColor == -2) {
                presColor = DEFAULT_QUICK_TILES_BG_PRESSED_COLOR;
            }
            ColorDrawable bgDrawable = new ColorDrawable(bgColor);
            ColorDrawable presDrawable = new ColorDrawable(presColor);
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[] {android.R.attr.state_pressed}, presDrawable);
            states.addState(new int[] {}, bgDrawable);
            mTile.setBackground(states);
        }
    }

    public void setRandomColor() {
        if(isRandom()) {
            Random generator = new Random();
            int color = mContext.getResources().getColor(RandomColors[generator.nextInt(RandomColors.length)]);
            mTile.setBackgroundColor(color);
        } else {
            setColor();
        }
    }
}
