package top.saymzx.easycontrol.app.helper;

import android.content.Context;
import android.content.SharedPreferences;

import top.saymzx.easycontrol.app.entity.AppData;

public class SharedPreferencesUtils {
    /**
     * 保存在手机里面的文件名
     */
    protected static String FILE_NAME = "app_settings";

    public static void setParam(String key, Object object) {
        setParam(AppData.applicationContext, key, object);
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void setParam(Context context, String key, Object object) {

        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if ("String".equals(type)) {
            editor.putString(key, (String) object);
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) object);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) object);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, (Float) object);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) object);
        }

        editor.apply();
    }

    public static String getStringParam(String key, String defaultObject) {
        return getParam(AppData.applicationContext, key, defaultObject);
    }

    public static String getParam(Context context, String key, String defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, defaultObject);
    }

    public static int getIntParam(String key, int defaultObject) {
        return getParam(AppData.applicationContext, key, defaultObject);
    }

    public static int getParam(Context context, String key, int defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, defaultObject);
    }

    public static boolean getBooleanParam(String key, boolean defaultObject) {
        return getParam(AppData.applicationContext, key, defaultObject);
    }

    public static boolean getParam(Context context, String key, boolean defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultObject);
    }

    public static float getFloatParam(String key, long defaultObject) {
        return getParam(AppData.applicationContext, key, defaultObject);
    }

    public static float getParam(Context context, String key, float defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getFloat(key, defaultObject);
    }

    public static long getLongParam(String key, long defaultObject) {
        return getParam(AppData.applicationContext, key, defaultObject);
    }

    public static long getParam(Context context, String key, long defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getLong(key, defaultObject);
    }

    public static void deleteParam(String key) {
        deleteParam(key);
    }

    public static void deleteParam(Context context, String key) {

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(key);

        editor.apply();
    }
}
