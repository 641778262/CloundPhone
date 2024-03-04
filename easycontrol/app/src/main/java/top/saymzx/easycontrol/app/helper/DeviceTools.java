package top.saymzx.easycontrol.app.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

import top.saymzx.easycontrol.app.entity.AppData;

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

}
