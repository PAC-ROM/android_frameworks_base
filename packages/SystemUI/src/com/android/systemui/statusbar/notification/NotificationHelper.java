/*
 * Copyright (C) 2014 ParanoidAndroid Project.
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

package com.android.systemui.statusbar.notification;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.BaseStatusBar.NotificationClicker;
import com.android.systemui.statusbar.NotificationData.Entry;

import java.util.List;

/**
 * Helper class
 * Provides some helper methods
 * Works as a bridge between Hover, Peek and their surrounding layers
 */
public class NotificationHelper {

    private BaseStatusBar mStatusBar;
    private Context mContext;
    private ActivityManager mActivityManager;

    /**
     * Creates a new instance
     * @Param context the current Context
     * @Param statusBar the current BaseStatusBar
     */
    public NotificationHelper(BaseStatusBar statusBar, Context context) {
        mContext = context;
        mStatusBar = statusBar;

        // we need to know which is the foreground app
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public String getForegroundPackageName() {
        List<RunningTaskInfo> taskInfo = mActivityManager.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getPackageName();
    }

    public NotificationClicker getNotificationClickListener(Entry entry, boolean floating) {
        NotificationClicker intent = null;
        final PendingIntent contentIntent = entry.notification.getNotification().contentIntent;
        if (contentIntent != null) {
            intent = mStatusBar.makeClicker(contentIntent,
                    entry.notification.getPackageName(), entry.notification.getTag(),
                    entry.notification.getId());
            boolean makeFloating = floating
                    // if the notification is from the foreground app, don't open in floating mode
                    && !entry.notification.getPackageName().equals(getForegroundPackageName())
                    // if user is on default launcher, don't open in floating window
                    && !isUserOnLauncher()
                    && openInFloatingMode();

            intent.makeFloating(makeFloating);
        }
        return intent;
    }

    public NotificationClicker getNotificationClickListenerForHalo(Entry entry) {
        NotificationClicker intent = null;
        final PendingIntent contentIntent = entry.notification.getNotification().contentIntent;
        if (contentIntent != null) {
            intent = mStatusBar.makeClicker(contentIntent,
                    entry.notification.getPackageName(), entry.notification.getTag(),
                    entry.notification.getId());
            boolean makeFloating =
                    // if the notification is from the foreground app, don't open in floating mode
                    !entry.notification.getPackageName().equals(getForegroundPackageName())
                    // if user is on default launcher, don't open in floating window
                    && !isUserOnLauncher();

            intent.makeFloating(makeFloating);
        }
        return intent;
    }

    public boolean openInFloatingMode() {
        return Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.HEADS_UP_FLOATING_WINDOW, true);
    }

    public boolean isUserOnLauncher() {
        // Get default launcher name
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(intent,
                                              PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;

        // compare and return result
        return getForegroundPackageName().equals(currentHomePackage);
    }
}
