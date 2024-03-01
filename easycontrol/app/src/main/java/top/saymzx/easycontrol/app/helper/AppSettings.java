package top.saymzx.easycontrol.app.helper;

public class AppSettings {
    private static final String KEY_VOICE = "key_voice";
    private static final String KEY_VIRTUAL_KEYS = "key_virtual_keys";
    private static final String KEY_RESOLUTION = "key_resolution";
    private static final int RESOLUTION_SUPER = 3;
    private static final int RESOLUTION_HIGH = 2;
    private static final int RESOLUTION_COMMON = 1;

    private static boolean showVoice;
    private static boolean showVirtualKeys;
    private static int resolutionType;


    public static void initAppSettings() {
        showVoice = SharedPreferencesUtils.getBooleanParam(KEY_VOICE, true);
        showVirtualKeys = SharedPreferencesUtils.getBooleanParam(KEY_VIRTUAL_KEYS, true);
        resolutionType = SharedPreferencesUtils.getIntParam(KEY_RESOLUTION, 0);
    }

    public static boolean showVoice() {
        return showVoice;
    }

    public static void setShowVoice(boolean showVoice) {
        AppSettings.showVoice = showVoice;
        SharedPreferencesUtils.setParam(KEY_VOICE,showVoice);
    }

    public static boolean showVirtualKeys() {
        return showVirtualKeys;
    }

    public static void setShowVirtualKeys(boolean showVirtualKeys) {
        AppSettings.showVirtualKeys = showVirtualKeys;
        SharedPreferencesUtils.setParam(KEY_VIRTUAL_KEYS,showVirtualKeys);
    }

    public static int getResolutionType() {
        return resolutionType;
    }

    public static void setResolutionType(int resolutionType) {
        AppSettings.resolutionType = resolutionType;
        SharedPreferencesUtils.setParam(KEY_RESOLUTION,resolutionType);
    }
}
