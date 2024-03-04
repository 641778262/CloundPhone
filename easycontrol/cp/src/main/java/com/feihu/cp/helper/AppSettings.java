package com.feihu.cp.helper;

import android.os.SystemClock;

public class AppSettings {
    private static final String KEY_VOICE = "key_voice";
    private static final String KEY_VIRTUAL_KEYS = "key_virtual_keys";
    private static final String KEY_RESOLUTION = "key_resolution";

    public static final long AUTO_DISCONNECT_TIME = 2 * 60 * 1000;//无操作自动断开时间

    private static final int RESOLUTION_SUPER = 3;
    private static final int RESOLUTION_HIGH = 2;
    private static final int RESOLUTION_COMMON = 1;

    public static boolean sConnected;//是否已经连接成功
    public static long sLastTouchTime;//上次操作云手机的时间
    private static boolean sShowVoice;
    private static boolean sShowVirtualKeys;
    private static int sResolutionType;


    public static void initAppSettings() {
        sShowVoice = SharedPreferencesUtils.getBooleanParam(KEY_VOICE, true);
        sShowVirtualKeys = SharedPreferencesUtils.getBooleanParam(KEY_VIRTUAL_KEYS, true);
        sResolutionType = SharedPreferencesUtils.getIntParam(KEY_RESOLUTION, 0);
    }

    public static boolean showVoice() {
        return sShowVoice;
    }

    public static void setShowVoice(boolean showVoice) {
        sShowVoice = showVoice;
        SharedPreferencesUtils.setParam(KEY_VOICE, sShowVoice);
    }

    public static boolean showVirtualKeys() {
        return sShowVirtualKeys;
    }

    public static void setShowVirtualKeys(boolean showVirtualKeys) {
        sShowVirtualKeys = showVirtualKeys;
        SharedPreferencesUtils.setParam(KEY_VIRTUAL_KEYS, sShowVirtualKeys);
    }

    public static int getResolutionType() {
        return sResolutionType;
    }

    public static void setResolutionType(int resolutionType) {
        sResolutionType = resolutionType;
        SharedPreferencesUtils.setParam(KEY_RESOLUTION, sResolutionType);
    }

    public static void resetLastTouchTime() {
        sLastTouchTime = SystemClock.elapsedRealtime();
    }

    public static boolean showTimeOutDialog() {
        return sConnected && SystemClock.elapsedRealtime() - sLastTouchTime > AUTO_DISCONNECT_TIME;
    }
}
