package com.feihu.cp.helper;

import android.os.SystemClock;


public class AppSettings {
    private static final String KEY_VOICE = "key_voice";
    private static final String KEY_VIRTUAL_KEYS = "key_virtual_keys";
    private static final String KEY_RESOLUTION = "key_resolution";
    private static final String KEY_MOBILE_NET_TIPS = "key_mobile_net_tips";
    private static final String KEY_FULL_SCREEN = "key_full_screen";
    private static final String KEY_BACK_CONFIRM = "key_back_confirm";
    private static final String KEY_CONTROL_MODE = "key_control_mode";
    private static final String KEY_DEVICE_MATCH_PARENT = "key_device_match_parent";

    public static final long AUTO_DISCONNECT_TIME = 10 * 60 * 1000;//无操作自动断开时间
    public static final long BACK_DISCONNECT_TIME = 90 * 1000;//退到后台自动断开时间

    public static final int RESOLUTION_SUPER = 3;
    public static final int RESOLUTION_HIGH = 2;
    public static final int RESOLUTION_COMMON = 1;
    private static final int BIT_COMMON = 1;
    private static final int BIT_HIGH = 2;
    private static final int BIT_SUPER = 4;
    private static final int FPS_COMMON = 90;
    private static final int FPS_HIGH = 90;
    private static final int FPS_SUPER = 90;
    public static final int CONTROL_MODE_DEFAULT = 1;
    public static final int CONTROL_MODE_PROFESSIONAL = 2;

    public static boolean sPaused;//退出到桌面
    public static boolean sConnected;//是否已经连接成功
    public static long sLastTouchTime;//上次操作云手机的时间
    private static boolean sShowVoice;
    private static boolean sShowVirtualKeys;
    private static int sResolutionType;
    private static boolean sShowMobileNetTips;

//    private static boolean sFullScreen;
    private static boolean sBackConfirm;
    private static int sControlMode;
    private static boolean sDeviceMatchParent;//是否满屏拉伸显示云手机
    public static boolean sUniApp;//是否通过uniApp访问


    public static void initAppSettings() {
        sShowVoice = SharedPreferencesUtils.getBooleanParam(KEY_VOICE, true);
        sShowVirtualKeys = SharedPreferencesUtils.getBooleanParam(KEY_VIRTUAL_KEYS, true);
        sResolutionType = SharedPreferencesUtils.getIntParam(KEY_RESOLUTION, RESOLUTION_COMMON);
        sShowMobileNetTips = SharedPreferencesUtils.getBooleanParam(KEY_MOBILE_NET_TIPS, true);
//        sFullScreen = SharedPreferencesUtils.getBooleanParam(KEY_FULL_SCREEN, true);
        sBackConfirm = SharedPreferencesUtils.getBooleanParam(KEY_BACK_CONFIRM, true);
        sControlMode = SharedPreferencesUtils.getIntParam(KEY_CONTROL_MODE, CONTROL_MODE_DEFAULT);
        sDeviceMatchParent = SharedPreferencesUtils.getBooleanParam(KEY_DEVICE_MATCH_PARENT, true);
    }

    public static boolean isDeviceMatchParent() {
        return sDeviceMatchParent;
    }

    public static void setDeviceMatchParent(boolean deviceMatchParent) {
        sDeviceMatchParent = deviceMatchParent;
        SharedPreferencesUtils.setParam(KEY_DEVICE_MATCH_PARENT, deviceMatchParent);
    }


    public static boolean isDefaultControlMode() {
        return sControlMode == CONTROL_MODE_DEFAULT;
    }

    public static void setControlMode(int controlMode) {
        sControlMode = controlMode;
        SharedPreferencesUtils.setParam(KEY_CONTROL_MODE, controlMode);
    }

//    public static boolean isFullScreen() {
//        return sFullScreen;
//    }
//
//    public static void setFullScreen(boolean fullScreen) {
//        sFullScreen = fullScreen;
//        SharedPreferencesUtils.setParam(KEY_FULL_SCREEN, fullScreen);
//    }

    public static boolean isBackConfirm() {
        return sBackConfirm;
    }

    public static void setBackConfirm(boolean backConfirm) {
        sBackConfirm = backConfirm;
        SharedPreferencesUtils.setParam(KEY_BACK_CONFIRM, backConfirm);
    }

    public static boolean showMobileNetTips() {
        return sShowMobileNetTips;
    }

    public static void setShowMobileNetTips(boolean showMobileNetTips) {
        sShowMobileNetTips = showMobileNetTips;
        SharedPreferencesUtils.setParam(KEY_MOBILE_NET_TIPS, showMobileNetTips);
    }

    public static boolean showVoice() {
        return sShowVoice && !AppSettings.sPaused;
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

    public static int getVideoBits() {
        switch (sResolutionType) {
            case RESOLUTION_SUPER:
                return BIT_SUPER;
            case RESOLUTION_HIGH:
                return BIT_HIGH;
        }
        return BIT_COMMON;
    }

    public static int getVideoFps() {
        switch (sResolutionType) {
            case RESOLUTION_SUPER:
                return FPS_SUPER;
            case RESOLUTION_HIGH:
                return FPS_HIGH;
        }
        return FPS_COMMON;
    }

    public static void resetLastTouchTime() {
        sLastTouchTime = SystemClock.elapsedRealtime();
    }

    public static boolean showTimeOutDialog() {
        long autoDisconnectTime = sPaused ? BACK_DISCONNECT_TIME : AUTO_DISCONNECT_TIME;
        return sConnected && SystemClock.elapsedRealtime() - sLastTouchTime > autoDisconnectTime;
    }
}
