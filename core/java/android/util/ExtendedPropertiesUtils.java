/*
 * Copyright (C) 2012 ParanoidAndroid Project
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

package android.util;

import android.app.ActivityManager;
import android.os.SystemProperties;
import android.util.Log;
import android.content.Context;
import android.content.pm.*;
import android.app.*;
import android.content.res.Resources;
import android.content.res.CompatibilityInfo;

import java.io.*;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.ArrayList;

public class ExtendedPropertiesUtils {

    public static class ParanoidAppInfo {
        public boolean Active = false;
        public int Pid = 0;
        public ApplicationInfo Info = null;
        public String Name = "";
        public String Path = "";
        public int Mode = 0;
        public int ScreenWidthDp = 0;
        public int ScreenHeightDp = 0;
        public int ScreenLayout = 0;
        public int Dpi = 0;
        public float ScaledDensity = 0;
        public float Density = 0;
    }

    // STATIC PROPERTIES
    public static final String PARANOID_PROPIERTIES = "/system/pad.prop";
    public static final String PARANOID_PREFIX = "%";

    public static ActivityThread mParanoidMainThread = null;
    public static Context mParanoidContext = null;
    public static PackageManager mParanoidPackageManager = null;
    public static List<PackageInfo> mParanoidPackageList;
    public static HashMap<String, String> mPropertyMap = new HashMap<String, String>();

    public static int mParanoidScreen1Width = 0;
    public static int mParanoidScreen1Height = 0;
    public static int mParanoidScreen1Layout = 0;
    public static int mParanoidScreen2Width = 0;
    public static int mParanoidScreen2Height = 0;
    public static int mParanoidScreen2Layout = 0;
    public static int mParanoidScreen3Width = 0;
    public static int mParanoidScreen3Height = 0;
    public static int mParanoidScreen3Layout = 0;
    public static int mParanoidRomTabletBase = 0;
    public static int mParanoidRomPhoneBase = 0;
    public static int mParanoidRomCurrentBase = 0;
    public static int mParanoidRomLcdDensity = DisplayMetrics.DENSITY_DEFAULT;

    public static ParanoidAppInfo mParanoidGlobalHook = new ParanoidAppInfo();
    public ParanoidAppInfo mParanoidLocalHook = new ParanoidAppInfo();

    public static boolean mIsTablet;

    public static native String readFile(String s);

    // TODO: Port to native code
    public static void getTabletModeStatus(){
        mIsTablet = Integer.parseInt(getProperty("com.android.systemui.mode")) > 1;
    }

    // SET UP HOOK BY READING OUT PAD.PROP
    public static void paranoidConfigure(ParanoidAppInfo Info) {

        // FETCH DEFAUTS
        boolean isSystemApp = Info.Path.contains("system/app");
        int DefaultDpi = Integer.parseInt(getProperty(PARANOID_PREFIX + (isSystemApp ? "system_default_dpi" : "user_default_dpi"), "0"));
        int DefaultMode = isSystemApp == false ? 
            Integer.parseInt(getProperty(PARANOID_PREFIX + "user_default_mode", "0")) : 0;

        // CONFIGURE LAYOUT
        Info.Mode = Integer.parseInt(getProperty(Info.Name + ".mode", String.valueOf(DefaultMode)));
        switch (Info.Mode) {
            case 1:  
                Info.ScreenWidthDp = mParanoidScreen1Width;
                Info.ScreenHeightDp = mParanoidScreen1Height;
                Info.ScreenLayout = mParanoidScreen1Layout;
                break;
            case 2: 
                Info.ScreenWidthDp = mParanoidScreen2Width;
                Info.ScreenHeightDp = mParanoidScreen2Height;
                Info.ScreenLayout = mParanoidScreen2Layout;
                break;
            case 3: 
                Info.ScreenWidthDp = mParanoidScreen3Width;
                Info.ScreenHeightDp = mParanoidScreen3Height;
                Info.ScreenLayout = mParanoidScreen3Layout;
                break;
        }

        // CONFIGURE DPI
        Info.Dpi = Integer.parseInt(getProperty(Info.Name + ".dpi", String.valueOf(DefaultDpi)));

        // CONFIGURE DENSITIES
        Info.Density = Float.parseFloat(getProperty(Info.Name + ".den", "0"));
        Info.ScaledDensity = Float.parseFloat(getProperty(Info.Name + ".sden", "0"));

        // CALCULATE RELATIONS, IF NEEDED
        if (Info.Dpi != 0) {			
            Info.Density = Info.Density == 0 ? Info.Dpi / (float) DisplayMetrics.DENSITY_DEFAULT : Info.Density;
			Info.ScaledDensity = Info.ScaledDensity == 0 ? Info.Dpi / (float) DisplayMetrics.DENSITY_DEFAULT : Info.ScaledDensity;
        }

        // FLAG AS READY TO GO
        Info.Active = true;
    }

    public boolean paranoidOverride(ApplicationInfo Info) {
        if (paranoidIsInitialized() && Info != null) {
            mParanoidLocalHook.Pid = android.os.Process.myPid();
            mParanoidLocalHook.Info = Info;
            if (mParanoidLocalHook.Info != null) {
                mParanoidLocalHook.Name = mParanoidLocalHook.Info.packageName;
                mParanoidLocalHook.Path = mParanoidLocalHook.Info.sourceDir.substring(0, 
                    mParanoidLocalHook.Info.sourceDir.lastIndexOf("/"));
                paranoidConfigure(mParanoidLocalHook);
            }
            return true;
        }
        return false;
    }

    // COMPONENTS CAN OVERRIDE THEIR PROCESS-HOOK
    public boolean paranoidOverride(String Fullname) {
        if (paranoidIsInitialized() && Fullname != null) {
            mParanoidLocalHook.Pid = android.os.Process.myPid();
            mParanoidLocalHook.Info = getAppInfoFromPath(Fullname);
            if (mParanoidLocalHook.Info != null) {
                mParanoidLocalHook.Name = mParanoidLocalHook.Info.packageName;
                mParanoidLocalHook.Path = mParanoidLocalHook.Info.sourceDir.substring(0,
                    mParanoidLocalHook.Info.sourceDir.lastIndexOf("/"));
                paranoidConfigure(mParanoidLocalHook);
            }
            return true;
        }
        return false;
    }

    // COMPONENTS CAN OVERRIDE THEIR PROCESS-HOOK
    public boolean paranoidOverrideAndExclude(String Fullname) {
        ApplicationInfo tempInfo = getAppInfoFromPath(Fullname);
        if (tempInfo != null && (!paranoidIsHooked() || getProperty(tempInfo.packageName + ".force", "0").equals("1"))) {
            mParanoidLocalHook.Pid = android.os.Process.myPid();
            mParanoidLocalHook.Info = tempInfo;
            mParanoidLocalHook.Name = mParanoidLocalHook.Info.packageName;
            mParanoidLocalHook.Path = mParanoidLocalHook.Info.sourceDir.substring(0,
                mParanoidLocalHook.Info.sourceDir.lastIndexOf("/"));
            paranoidConfigure(mParanoidLocalHook);
            return true;
        }
        return false;
    }

    // COMPONENTS CAN COPY ANOTHER COMPONENTS HOOK
    public boolean paranoidOverride(ExtendedPropertiesUtils New) {
        if (paranoidIsInitialized() && New != null && New.mParanoidLocalHook.Active) {
            mParanoidLocalHook.Active = New.mParanoidLocalHook.Active;
            mParanoidLocalHook.Pid = New.mParanoidLocalHook.Pid;
            mParanoidLocalHook.Info = New.mParanoidLocalHook.Info;
            mParanoidLocalHook.Name = New.mParanoidLocalHook.Name;
            mParanoidLocalHook.Path = New.mParanoidLocalHook.Path;
            mParanoidLocalHook.Mode = New.mParanoidLocalHook.Mode;
            mParanoidLocalHook.ScreenWidthDp = New.mParanoidLocalHook.ScreenWidthDp;
            mParanoidLocalHook.ScreenHeightDp = New.mParanoidLocalHook.ScreenHeightDp;
            mParanoidLocalHook.ScreenLayout = New.mParanoidLocalHook.ScreenLayout;
            mParanoidLocalHook.Dpi = New.mParanoidLocalHook.Dpi;
            mParanoidLocalHook.ScaledDensity = New.mParanoidLocalHook.ScaledDensity;
            mParanoidLocalHook.Density = New.mParanoidLocalHook.Density;
            return true;
        }
        return false;
    }

    static public boolean paranoidIsInitialized() {
        return (mParanoidContext != null);
    }
    static public boolean paranoidIsHooked() {
        return (paranoidIsInitialized() && !mParanoidGlobalHook.Name.equals("android") && !mParanoidGlobalHook.Name.equals(""));
    }
    public boolean paranoidGetActive() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Active : mParanoidGlobalHook.Active;
    }
    public int paranoidGetPid() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Pid : mParanoidGlobalHook.Pid;
    }
    public ApplicationInfo paranoidGetInfo() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Info : mParanoidGlobalHook.Info;
    }
    public String paranoidGetName() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Name : mParanoidGlobalHook.Name;
    }
    public String paranoidGetPath() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Path : mParanoidGlobalHook.Path;
    }
    public int paranoidGetMode() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Mode : mParanoidGlobalHook.Mode;
    }
    public int paranoidGetScreenWidthDp() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.ScreenWidthDp : mParanoidGlobalHook.ScreenWidthDp;
    }
    public int paranoidGetScreenHeightDp() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.ScreenHeightDp : mParanoidGlobalHook.ScreenHeightDp;
    }
    public int paranoidGetScreenLayout() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.ScreenLayout : mParanoidGlobalHook.ScreenLayout;
    }
    public int paranoidGetDpi() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Dpi : mParanoidGlobalHook.Dpi;
    }
    public float paranoidGetScaledDensity() { 
        return mParanoidLocalHook.Active ? mParanoidLocalHook.ScaledDensity : mParanoidGlobalHook.ScaledDensity;
    }
    public float paranoidGetDensity() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Density : mParanoidGlobalHook.Density;
    }

    public static ApplicationInfo getAppInfoFromPath(String Path) {
        if(paranoidIsInitialized()) {
            for(int i=0; mParanoidPackageList != null && i<mParanoidPackageList.size(); i++) {
                PackageInfo p = mParanoidPackageList.get(i);
                if (p.applicationInfo != null && p.applicationInfo.sourceDir.equals(Path))
                    return p.applicationInfo;
            }
        }
        return null;
    }

    public static ApplicationInfo getAppInfoFromPackageName(String PackageName) {
        if(paranoidIsInitialized()) {
            for(int i=0; mParanoidPackageList != null && i<mParanoidPackageList.size(); i++) {
                PackageInfo p = mParanoidPackageList.get(i);
                if (p.applicationInfo != null && p.applicationInfo.packageName.equals(PackageName))
                    return p.applicationInfo;
            }
        }
        return null;
    }

    public static ApplicationInfo getAppInfoFromPID(int PID) {
        if (paranoidIsInitialized()) {
            List mProcessList = ((ActivityManager)mParanoidContext.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            Iterator mProcessListIt = mProcessList.iterator();
            while(mProcessListIt.hasNext()) {
                ActivityManager.RunningAppProcessInfo mAppInfo = (ActivityManager.RunningAppProcessInfo)(mProcessListIt.next());
                if(mAppInfo.pid == PID)
                    return getAppInfoFromPackageName(mAppInfo.processName);
            }
        }
        return null;
    }

    public static String paranoidStatus() {
        return " T:" + (mParanoidMainThread != null) + " CXT:" + (mParanoidContext != null) + " PM:" + (mParanoidPackageManager != null);
    }

    // TODO: Port to native code
    public void paranoidLog(String Message) {
        Log.i("PARANOID:" + Message, "Init=" + (mParanoidMainThread != null && mParanoidContext != null && 
            mParanoidPackageManager != null) + " App=" + paranoidGetName() + " Dpi=" + paranoidGetDpi() + 
            " Mode=" + paranoidGetMode());
    }

    public void paranoidTrace(String Message) {
        StringWriter sw = new StringWriter();
        new Throwable("").printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        Log.i("PARANOID:" + Message, "Trace=" + stackTrace); 
    }

    public static void refreshProperties() {
        mPropertyMap.clear();
        String[] props = readFile(PARANOID_PROPIERTIES).split("\n");
        for(int i=0; i<props.length; i++) {
            if (!props[i].startsWith("#")) {
                String[] pair = props[i].split("=");
                if (pair.length == 2)
                    mPropertyMap.put(pair[0].trim(), pair[1].trim());
            }
        }
    }

    // TODO: Port to native code
    public static String getProperty(String prop){
        return getProperty(prop, "0");
    }

    // TODO: Port to native code
    public static String getProperty(String prop, String def) {
        try {
            if (paranoidIsInitialized()) {
                String result = mPropertyMap.get(prop);
                if (result == null)
                    return def;
                if (result.startsWith(PARANOID_PREFIX))
		    result = getProperty(result, def);
		return result;	
            } else {
                String[] props = readFile(PARANOID_PROPIERTIES).split("\n");
                for(int i=0; i<props.length; i++) {
                    if(props[i].contains("=")) {
                        if(props[i].substring(0, props[i].lastIndexOf("=")).equals(prop)) {
                            String result = props[i].replace(prop+"=", "").trim();  
                            if (result.startsWith(PARANOID_PREFIX))
                                result = getProperty(result, def);
                            return result;  
                        }
                    }
                }
                return def;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return def;
    }
}
