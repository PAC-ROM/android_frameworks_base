/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.systemui.statusbar.phone;

import com.android.systemui.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.android.systemui.quicksettings.QuickSettingsTile;
import com.android.internal.util.omni.ColorUtils;

/**
 *
 */
public class QuickSettingsTileView extends FrameLayout {
    private static final String TAG = "QuickSettingsTileView";

    private int mContentLayoutId;
    private int mColSpan;
    private boolean mPrepared;
    private OnPrepareListener mOnPrepareListener;
    private QuickSettingsTile mTile;

    private Context mContext;
    private int mCurrentBgColor = -3;
    private int mCurrentTextColor = -3;
    
    public QuickSettingsTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mContentLayoutId = -1;
        mColSpan = 1;
    }

    public void setTile(QuickSettingsTile tile) {
        mTile = tile;
    }

    public QuickSettingsTile getTile() {
        return mTile;
    }

    void setColumnSpan(int span) {
        mColSpan = span;
    }

    int getColumnSpan() {
        return mColSpan;
    }

    public void setContent(int layoutId, LayoutInflater inflater) {
        mContentLayoutId = layoutId;
        inflater.inflate(layoutId, this);
    }

    void reinflateContent(LayoutInflater inflater) {
        if (mContentLayoutId != -1) {
            removeAllViews();
            setContent(mContentLayoutId, inflater);
        } else {
            Log.e(TAG, "Not reinflating content: No layoutId set");
        }
    }

    protected int getDefaultColor() {
        return mContext.getResources().getColor(R.color.qs_textview_color);
    }

    protected int getCurrentColor() {
        return mCurrentTextColor;
    }

    private Drawable getGradientDrawable(boolean isNav, int color) {
        if (isNav) {
            if (ColorUtils.isBrightColor(color)) {
                color = ColorUtils.darken(color, 0.5f);
            } else {
                color = ColorUtils.lighten(color, 0.5f);
            }
        }
        GradientDrawable drawable = new GradientDrawable(Orientation.TOP_BOTTOM,
                                     new int[]{color, color});
        drawable.setDither(true);
        color = ColorUtils.changeColorTransparency(color, 100);
        drawable.setStroke(5, color);
        return drawable;
    }

    private StateListDrawable getStateListDrawable(int color) {
        Drawable drawableNr = getGradientDrawable(false, color);
        Drawable drawablePs = getGradientDrawable(true, color);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[] { android.R.attr.state_pressed }, drawablePs);
        stateListDrawable.addState(new int[0], drawableNr);
        return stateListDrawable;
    }

    public void changeCurrentBackground(boolean enabled) {
        if (mCurrentBgColor != -3) {
            if (enabled) {
                setBackgroundResource(R.drawable.qs_tile_background_no_hover);
            } else {
                setBackground(getStateListDrawable(mCurrentBgColor));
            }
        } else {
            setBackgroundResource(enabled ? R.drawable.qs_tile_background_no_hover :
                   R.drawable.qs_tile_background);
        }
        changeCurrentUiColor(enabled ? -3 : mCurrentTextColor);
    }

    protected void changeCurrentUiColor(int ic_color) {
        // this will call changing Ui color on child views when edit mode enabled
    }

    protected int getCurrentBgColor() {
        return mCurrentBgColor;
    }

    public void changeColorIconBackground(int bg_color, int ic_color) {
        mCurrentBgColor = bg_color;
        mCurrentTextColor = ic_color;
        if (bg_color != -3) {
            setBackground(getStateListDrawable(bg_color));
        } else {
            setBackgroundResource(R.drawable.qs_tile_background);
        }
    }
    
    @Override
    public void setVisibility(int vis) {
        if (QuickSettings.DEBUG_GONE_TILES) {
            if (vis == View.GONE) {
                vis = View.VISIBLE;
                setAlpha(0.25f);
                setEnabled(false);
            } else {
                setAlpha(1f);
                setEnabled(true);
            }
        }
        super.setVisibility(vis);
    }

    public void setOnPrepareListener(OnPrepareListener listener) {
        if (mOnPrepareListener != listener) {
            mOnPrepareListener = listener;
            mPrepared = false;
            post(new Runnable() {
                @Override
                public void run() {
                    updatePreparedState();
                }
            });
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updatePreparedState();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updatePreparedState();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        updatePreparedState();
    }

    private void updatePreparedState() {
        if (mOnPrepareListener != null) {
            if (isParentVisible()) {
                if (!mPrepared) {
                    mPrepared = true;
                    mOnPrepareListener.onPrepare();
                }
            } else if (mPrepared) {
                mPrepared = false;
                mOnPrepareListener.onUnprepare();
            }
        }
    }

    private boolean isParentVisible() {
        if (!isAttachedToWindow()) {
            return false;
        }
        for (ViewParent current = getParent(); current instanceof View;
                current = current.getParent()) {
            View view = (View)current;
            if (view.getVisibility() != VISIBLE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called when the view's parent becomes visible or invisible to provide
     * an opportunity for the client to provide new content.
     */
    public interface OnPrepareListener {
        void onPrepare();
        void onUnprepare();
    }
}