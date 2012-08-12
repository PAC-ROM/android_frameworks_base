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
import android.view.Display;
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
        public int Dpi = 0;
        public int Layout = 0;
        public int Force = 0;
        public int Large = 0;
        public float ScaledDensity = 0;
        public float Density = 0;
    }

    public static enum OverrideMode {
        ExtendedProperties, AppInfo, Fullname, FullnameExclude, PackageName
    }

    // STATIC PROPERTIES
    public static final String PARANOID_PROPERTIES = "/system/etc/paranoid/properties.conf";
    public static final String PARANOID_DIR = "/system/etc/paranoid/";
    public static final String PARANOID_MAINCONF = "properties.conf";
    public static final String PARANOID_BACKUPCONF = "backup.conf";
    public static final String PARANOID_PREFIX = "%";

    public static ActivityThread mParanoidMainThread = null;
    public static Context mParanoidContext = null;
    public static PackageManager mParanoidPackageManager = null;    
    public static Display mParanoidDisplay = null;
    public static List<PackageInfo> mParanoidPackageList;
    public static HashMap<String, String> mPropertyMap = new HashMap<String, String>();

    public static ParanoidAppInfo mParanoidGlobalHook = new ParanoidAppInfo();
    public ParanoidAppInfo mParanoidLocalHook = new ParanoidAppInfo();

    public static boolean mIsTablet;
    public static int mParanoidRomLcdDensity = DisplayMetrics.DENSITY_DEFAULT;

    public static native String readFile(String s);

    // SET UP HOOK BY READING OUT PAD.PROP
    public static void paranoidConfigure(ParanoidAppInfo Info) {

        // FETCH DEFAUTS
        boolean isSystemApp = Info.Path.contains("system/app");
        int DefaultDpi = Integer.parseInt(getProperty(PARANOID_PREFIX + (isSystemApp ? 
            "system_default_dpi" : "user_default_dpi"), "0"));
        int DefaultLayout = Integer.parseInt(getProperty(PARANOID_PREFIX + (isSystemApp ? 
            "system_default_layout" : "user_default_layout"), "0"));

        // CONFIGURE LAYOUT
        Info.Layout = Integer.parseInt(getProperty(Info.Name + ".layout", String.valueOf(DefaultLayout)));

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

        // FORCE & LARGE
        Info.Force = Integer.parseInt(getProperty(Info.Name + ".force", "0"));
        Info.Large = Integer.parseInt(getProperty(Info.Name + ".large", "0"));

        // FLAG AS READY TO GO
        Info.Active = true;
    }

    public void paranoidOverride(Object input, OverrideMode mode) {
        if (paranoidIsInitialized() && input != null ) {

            ApplicationInfo tempInfo;
            ExtendedPropertiesUtils tempProps;

            switch (mode) {
                case ExtendedProperties:
                    tempProps = (ExtendedPropertiesUtils)input;
                    if (tempProps.mParanoidLocalHook.Active) {
                        mParanoidLocalHook.Active = tempProps.mParanoidLocalHook.Active;
                        mParanoidLocalHook.Pid = tempProps.mParanoidLocalHook.Pid;
                        mParanoidLocalHook.Info = tempProps.mParanoidLocalHook.Info;
                        mParanoidLocalHook.Name = tempProps.mParanoidLocalHook.Name;
                        mParanoidLocalHook.Path = tempProps.mParanoidLocalHook.Path;
                        mParanoidLocalHook.Layout = tempProps.mParanoidLocalHook.Layout;
                        mParanoidLocalHook.Dpi = tempProps.mParanoidLocalHook.Dpi;
                        mParanoidLocalHook.Force = tempProps.mParanoidLocalHook.Force;
                        mParanoidLocalHook.Large = tempProps.mParanoidLocalHook.Large;
                        mParanoidLocalHook.ScaledDensity = tempProps.mParanoidLocalHook.ScaledDensity;
                        mParanoidLocalHook.Density = tempProps.mParanoidLocalHook.Density;                        
                    }
                    return;
                case AppInfo:
                    mParanoidLocalHook.Info = (ApplicationInfo)input;
                    break;
                case Fullname:
                    mParanoidLocalHook.Info = getAppInfoFromPath((String)input);
                    break;
                case FullnameExclude:
                    tempInfo = getAppInfoFromPath((String)input);
                    if (tempInfo != null && (!paranoidIsHooked() || getProperty(tempInfo.packageName + ".force", "0").equals("1")))
                        mParanoidLocalHook.Info = tempInfo;
                    break;
                case PackageName:
                    mParanoidLocalHook.Info = getAppInfoFromPackageName((String)input);
                    break;
            }

            if (mParanoidLocalHook.Info != null) {
                mParanoidLocalHook.Pid = android.os.Process.myPid();
                mParanoidLocalHook.Name = mParanoidLocalHook.Info.packageName;
                mParanoidLocalHook.Path = mParanoidLocalHook.Info.sourceDir.substring(0, 
                    mParanoidLocalHook.Info.sourceDir.lastIndexOf("/"));
                paranoidConfigure(mParanoidLocalHook);
            }            
        }
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
    public int paranoidGetLayout() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Layout : mParanoidGlobalHook.Layout;
    }
    public int paranoidGetDpi() {
        return mParanoidLocalHook.Active ? mParanoidLocalHook.Dpi : mParanoidGlobalHook.Dpi;
    }
    public float paranoidGetScaledDensity() { 
        return mParanoidLocalHook.Active ? mParanoidLocalHook.ScaledDensity : mParanoidGlobalHook.ScaledDensity;
    }
    public boolean paranoidGetForce() {
        return (mParanoidLocalHook.Active ? mParanoidLocalHook.Force : mParanoidGlobalHook.Force) == 1;
    }
    public boolean paranoidGetLarge() {
        return (mParanoidLocalHook.Active ? mParanoidLocalHook.Large : mParanoidGlobalHook.Large) == 1;
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
            " Layout=" + paranoidGetLayout());
    }

    public static void paranoidTrace(String Message) {
        StringWriter sw = new StringWriter();
        new Throwable("").printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        Log.i("PARANOID:" + Message, "Trace=" + stackTrace); 
    }

    public static void refreshProperties() {
        mPropertyMap.clear();
        String[] props = readFile(PARANOID_PROPERTIES).split("\n");
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
                String[] props = readFile(PARANOID_PROPERTIES).split("\n");
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

    public static int getValue(String r){
        try{
            int t = Integer.parseInt(getProperty(r));
            return t == 0 ? SystemProperties.getInt("ro.sf.lcd_density", 0) : t;
        } catch(NumberFormatException e){
            // Will never happen, hopefully
        }
        return 0;
    }
}
