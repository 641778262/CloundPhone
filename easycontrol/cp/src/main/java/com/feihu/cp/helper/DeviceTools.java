package com.feihu.cp.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.feihu.cp.entity.AppData;
import com.feihu.cp.entity.MyInterface;

public class DeviceTools {
    // 设置全面屏
    public static void setFullScreen(Activity context) {
        // 全屏显示
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        context.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        // 设置异形屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = context.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            context.getWindow().setAttributes(lp);
        }
    }

    public static void viewAnim(View view, boolean toShowView, int translationX, int translationY, MyInterface.MyFunctionBoolean action) {
        // 创建平移动画
        view.setTranslationX(toShowView ? translationX : 0);
        float endX = toShowView ? 0 : translationX;
        view.setTranslationY(toShowView ? translationY : 0);
        float endY = toShowView ? 0 : translationY;
        // 创建透明度动画
        view.setAlpha(toShowView ? 0f : 1f);
        float endAlpha = toShowView ? 1f : 0f;

        // 设置动画时长和插值器
        ViewPropertyAnimator animator = view.animate()
                .translationX(endX)
                .translationY(endY)
                .alpha(endAlpha)
                .setDuration(toShowView ? 300 : 200)
                .setInterpolator(toShowView ? new OvershootInterpolator() : new DecelerateInterpolator());
        animator.withStartAction(() -> {
            if (action != null) action.run(true);
        });
        animator.withEndAction(() -> {
            if (action != null) action.run(false);
        });

        // 启动动画
        animator.start();
    }

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
