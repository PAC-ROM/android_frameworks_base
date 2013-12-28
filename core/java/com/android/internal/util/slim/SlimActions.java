/*
* Copyright (C) 2013 SlimRoms Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.internal.util.slim;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.SearchManager;
import android.app.IUiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import android.widget.Toast;

import com.android.internal.statusbar.IStatusBarService;

import java.net.URISyntaxException;

public class SlimActions {

    public static void processAction(Context context, String action, boolean isLongpress) {
        if (action == null) {
            return;
        }

        if (action.equals(ButtonsConstants.ACTION_THEME_SWITCH)) {
            boolean state = context.getResources().getConfiguration().uiThemeMode
                    == Configuration.UI_THEME_MODE_HOLO_DARK;
            // Handle a switch change
            // we currently switch between holodark and hololight till either
            // theme engine is ready or lightheme is ready. Currently due of
            // missing light themeing hololight = system base theme
            final IUiModeManager uiModeManagerService = IUiModeManager.Stub.asInterface(
                    ServiceManager.getService(Context.UI_MODE_SERVICE));
            try {
                uiModeManagerService.setUiThemeMode(state
                        ? Configuration.UI_THEME_MODE_HOLO_LIGHT
                        : Configuration.UI_THEME_MODE_HOLO_DARK);
             } catch (RemoteException e) {
             }
             return;
        }
    }

}

