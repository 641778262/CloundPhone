package top.saymzx.easycontrol.app.helper;

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
}
