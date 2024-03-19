package com.feihu.cp.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import com.feihu.cp.R;
import com.feihu.cp.entity.AppData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;



/**
 * Created by json on 17/3/31.
 */

public class StatusBarUtil {
    private static final int TYPE_MIUI = 1;//MIUI
    private static final int TYPE_FLYME = 2;//魅族系统
    private static final int TYPE_M = 3;//android 6.0以上


    /**
     * 修改状态栏为全透明
     * @param activity
     */
    @TargetApi(19)
    public static boolean transparencyBar(Activity activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(AppData.applicationContext.getResources().getColor(R.color.phone_bg));
            return true;

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window =activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            return true;
        }
        return false;
    }

    /**
     *
     * 判断是否能够设置状态栏字体颜色
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     * @param activity
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    public static int statusBarFontMode(Activity activity, boolean dark) {
        int result=0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            if (MIUISetStatusBarMode(activity.getWindow(),dark)) {
//                result = TYPE_MIUI;
//            } else
            if (FlymeSetStatusBarMode(activity.getWindow(),dark)) {
                result = TYPE_FLYME;
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(dark) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View
                            .SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

                result = TYPE_M;
            }
        }
        return result;
    }

    /**
     * 清除MIUI或flyme或6.0以上版本状态栏黑色字体
     */
    public static void statusBarFontLightMode(Activity activity, int type) {
        if (type == TYPE_MIUI) {
            MIUISetStatusBarMode(activity.getWindow(), false);
        } else if (type == TYPE_FLYME) {
            FlymeSetStatusBarMode(activity.getWindow(), false);
        } else if (type == TYPE_M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    /**
     * 设置MIUI或flyme或6.0以上版本状态栏黑色字体
     * @param activity
     * @param type
     */
    public static void statusBarFontDarkMode(Activity activity, int type) {
        if (type == TYPE_MIUI) {
            MIUISetStatusBarMode(activity.getWindow(), true);
        } else if (type == TYPE_FLYME) {
            FlymeSetStatusBarMode(activity.getWindow(), true);
        } else if (type == TYPE_M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View
                    .SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }


    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     * @param window 需要设置的窗口
     * @param dark 是否把状态栏字体及图标颜色设置为深色
     * @return  boolean 成功执行返回true
     *
     */
    public static boolean FlymeSetStatusBarMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     * @param window 需要设置的窗口
     * @param dark 是否把状态栏字体及图标颜色设置为深色
     * @return  boolean 成功执行返回true
     *
     */
    public static boolean MIUISetStatusBarMode(Window window, boolean dark) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isMIUIV9()) {//MIUI9 采用了android6.0以上原生的设置方式
            return false;
        }
        boolean result = false;
        if (window != null) {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isMIUIV9()) {
//                if(dark) {
//                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//                } else {
//                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//                }
//                result = true;
//            } else {
                Class clazz = window.getClass();
                try {
                    int darkModeFlag = 0;
                    Class layoutParams = Class.forName("android.view" +
                            ".MiuiWindowManager$LayoutParams");
                    Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                    darkModeFlag = field.getInt(layoutParams);
                    Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                    if (dark) {
                        extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                    } else {
                        extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                    }
                    result = true;
                } catch (Exception e) {

                }
//            }
        }
        return result;
    }

    private static int mStatusBarHeight = 0;

    public static int getStatusBarHeight() {
        if(mStatusBarHeight > 0) {
            return mStatusBarHeight;
        }

        /**
         * 获取状态栏高度——方法1
         * */
        Context context = AppData.applicationContext;
        //获取status_bar_height资源的ID
        try {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                mStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
            if(mStatusBarHeight < 0) {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object object = clazz.newInstance();
                int height = Integer.parseInt(clazz.getField("status_bar_height")
                        .get(object).toString());
                mStatusBarHeight = context.getResources().getDimensionPixelSize(height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mStatusBarHeight < 0) {
            mStatusBarHeight = 50;
        }
        return mStatusBarHeight;

    }

//    public static void setViewPadding(View view) {
//        if (view == null) {
//            return;
//        }
//        if (view.getTag(R.id.tag_first) != null) {
//            return;
//        }
//
//        view.setPadding(view.getLeft(), view.getPaddingTop() + getStatusBarHeight()
//                , view.getPaddingRight(), view.getPaddingBottom());
//        view.setTag(R.id.tag_first, "");
//    }

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            String versionCode = prop.getProperty(KEY_MIUI_VERSION_CODE,null);
            String versionName = prop.getProperty(KEY_MIUI_VERSION_NAME,null);
            String internalStorage = prop.getProperty(KEY_MIUI_INTERNAL_STORAGE,null);
            return  !TextUtils.isEmpty(versionCode) || !TextUtils.isEmpty(versionName) || !TextUtils.isEmpty(internalStorage);
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean isMIUIV9() {
        final BuildProperties prop;
        try {
            prop = BuildProperties.newInstance();
            String versionName = prop.getProperty(KEY_MIUI_VERSION_NAME,null);
            return "V9".equalsIgnoreCase(versionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //设置透明状态栏
    public static boolean setStatusBar(Activity activity) {
        boolean hasTransparent = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            hasTransparent = true;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                try {
                    Window window = activity.getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.TRANSPARENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return hasTransparent;
    }

}
