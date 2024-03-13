package top.saymzx.easycontrol.app.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

import top.saymzx.easycontrol.app.entity.AppData;

public class DeviceTools {

    public static int dp2px(float dpValue) {
        float scale = AppData.applicationContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

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

    public static int getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) AppData.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnectedOrConnecting()) {
            return -1;
        }
        return info.getType();
    }

    public static boolean isWiFiNet() {
        return ConnectivityManager.TYPE_WIFI == getNetworkType();
    }

    public static boolean isMobileNet() {
        return ConnectivityManager.TYPE_MOBILE == getNetworkType();
    }
}
