package com.feihu.cp.entity;

import com.feihu.cp.R;

public class Device {
    public static final int TYPE_NETWORK = 1;
    public static final int TYPE_LINK = 2;

    public final String uuid;
    public final int type;
    public String name;
    public String address = "";
    public boolean isAudio = true;
    public int maxSize = 1600;
    public int maxFps = 60;
    public int maxVideoBit = 4;
    public boolean useH265 = true;
    public boolean connectOnStart = false;
    public boolean customResolutionOnConnect = false;
    public boolean wakeOnConnect = true;
    public boolean lightOffOnConnect = false;
    public boolean showNavBarOnConnect = true;
    public boolean changeToFullOnConnect = false;
    public boolean keepWakeOnRunning = true;
    public boolean changeResolutionOnRunning = false;
    public boolean smallToMiniOnRunning = false;
    public boolean fullToMiniOnRunning = true;
    public boolean miniTimeoutOnRunning = false;
    public boolean lockOnClose = true;
    public boolean lightOnClose = false;
    public boolean reconnectOnClose = false;

    public int customResolutionWidth = 1080;
    public int customResolutionHeight = 2400;
    public int smallX = 200;
    public int smallY = 200;
    public int smallLength = 800;
    public int miniY = 200;

    public long leftTime;
    public int connectType;
    public String sourceId;
    public String vipType;
    public String system;
    public static final int CONNECT_TYPE_NORMAL = 0;
    public static final int CONNECT_TYPE_CHANGE_RESOLUTION = 1;
    public static final int CONNECT_TYPE_RECONNECT = 2;
    public static final int CONNECT_TYPE_AUTO_CONNECT = 3;
    public static final int CONNECT_TYPE_CHANGE_NETWORK = 4;

    public static final String VIP = "vip";
    public static final String SVIP = "svip";
    public static final String XVIP = "xvip";
    public static final String SYSTEM_12 = "12";
    public static final String SYSTEM_12_1 = "12.1";

    public Device(String uuid, int type) {
        this.uuid = uuid;
        this.type = type;
        this.name = uuid;
    }

    public boolean isNetworkDevice() {
        return type == TYPE_NETWORK;
    }

    public boolean isLinkDevice() {
        return type == TYPE_LINK;
    }

    public int getVipResourceId() {
        if (SVIP.equals(vipType)) {
            return SYSTEM_12.equals(system)?R.drawable.svip12:R.drawable.svip12_1;
        } else if (XVIP.equals(vipType)) {
            return SYSTEM_12.equals(system)?R.drawable.xvip12:R.drawable.xvip12_1;
        }
        return SYSTEM_12.equals(system)?R.drawable.vip12:R.drawable.vip12_1;
    }

}
