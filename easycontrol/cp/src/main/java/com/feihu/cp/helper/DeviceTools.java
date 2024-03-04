package com.feihu.cp.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

import com.feihu.cp.entity.AppData;

public class DeviceTools {
    public static float getScreenDensity() {
        DisplayMetrics dm = AppData.applicationContext.getResources().getDisplayMetrics();
        return dm.density;
    }


    public static int getScreenHeight() {
        DisplayMetrics dm = AppData.applicationContext.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getScreenWidth() {
        DisplayMetrics dm = AppData.applicationContext.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) AppData.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    public static String getVersionName() {
        PackageManager pm = AppData.applicationContext.getPackageManager();
        String pkgName = AppData.applicationContext.getPackageName();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pkgInfo != null ? pkgInfo.versionName : "";
    }

    public static int getVersionCode() {
        PackageManager pm = AppData.applicationContext.getPackageManager();
        String pkgName = AppData.applicationContext.getPackageName();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pkgInfo != null ? pkgInfo.versionCode : 0;
    }
}
