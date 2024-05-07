package com.feihu.cp;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.feihu.cp.client.Client;
import com.feihu.cp.client.ClientController;
import com.feihu.cp.entity.AppData;
import com.feihu.cp.entity.Device;
import com.feihu.cp.helper.AppSettings;
import com.feihu.cp.helper.DeviceTools;
import com.feihu.cp.helper.ToastUtils;

import java.util.concurrent.TimeUnit;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class ClientModule extends UniModule {
    private static final String MSG = "msg";
    private static final String CODE = "code";
    private static final String CODE_SUCCESS = "success";
    private static final String CODE_FAIL = "fail";


    @UniJSMethod(uiThread = true)
    public void testContext(UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            if (mUniSDKInstance == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "mUniSDKInstance null");
            } else {
                Context context = mUniSDKInstance.getContext();
                if (context == null) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "mUniSDKInstance context null");
                } else {
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "mUniSDKInstance context not null");
                }
            }

        } catch (Exception e) {
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "mUniSDKInstance exception" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            e.printStackTrace();
        }
        callback.invoke(data);

    }

    @UniJSMethod(uiThread = true)
    public void initModule(UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            Context context = mUniSDKInstance.getContext();
            if (context == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "initModule fail context null");
            } else {
                AppData.init(context);
                AppSettings.sUniApp = true;
                data.put(CODE, CODE_SUCCESS);
                data.put(MSG, "initModule success");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "initModule exception:" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }

    }

    @UniJSMethod(uiThread = true)
    public void initAppSettings(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            if (params == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "initAppSettings params null");
            } else {
                if (!params.containsKey("voice") || !params.containsKey("fullScreen") ||
                        !params.containsKey("backConfirm") || !params.containsKey("mobileNetTips")) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "initAppSettings missing params");
                } else {
                    boolean voice = params.getBoolean("voice");
                    AppSettings.setShowVoice(voice);
                    boolean fullScreen = params.getBoolean("fullScreen");
//                    AppSettings.setFullScreen(fullScreen);
                    AppSettings.setControlMode(fullScreen?AppSettings.CONTROL_MODE_DEFAULT:AppSettings.CONTROL_MODE_PROFESSIONAL);
                    boolean backConfirm = params.getBoolean("backConfirm");
                    AppSettings.setBackConfirm(backConfirm);
                    boolean mobileNetTips = params.getBoolean("mobileNetTips");
                    AppSettings.setShowMobileNetTips(mobileNetTips);
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "initAppSettings success");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "initAppSettings exception" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }
    }

    @UniJSMethod(uiThread = true)
    public void openCloudPhonePort(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            if (params == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "openCloudPhonePort params null");
            } else {
                String uuid = params.getString("uuid");
                String address = params.getString("address");
                String name = params.getString("name");
                if (TextUtils.isEmpty(address) || TextUtils.isEmpty(uuid)) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "openCloudPhonePort address or uuid param empty");
                } else {
                    Device device = new Device(uuid, Device.TYPE_NETWORK);
                    device.address = address;
                    device.name = name;
                    DeviceTools.connectCloudPhone(mUniSDKInstance.getContext(), device);
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "openCloudPhonePort success");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "openCloudPhonePort exception" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }
    }


    //run ui thread
    @UniJSMethod(uiThread = true)
    public void connectCloudPhone(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            if (params == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "connectCloudPhone params null");
            } else {
                String code = params.getString("code");
                String address = params.getString("address");
                String name = params.getString("name");
                String uuid = params.getString("uuid");
                String sourceId = params.getString("machineCode");
                String vipType = params.getString("vipType");
                int leftTime = params.getIntValue("leftTime");
                if (TextUtils.isEmpty(address) || TextUtils.isEmpty(uuid)) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "connectCloudPhone address or uuid param empty");
                } else {
                    if (!CODE_SUCCESS.equals(code)) {
                        Client.dismissDialog();
//                        ToastUtils.showToastNoRepeat(R.string.connect_error);
                        data.put(CODE, CODE_FAIL);
                        data.put(MSG, "connectCloudPhone open port error");
                    } else {
                        Device existDevice = ClientController.getDevice(uuid);
                        if (existDevice == null) {
                            existDevice = new Device(uuid, Device.TYPE_NETWORK);
                        }
                        existDevice.address = address;
                        existDevice.name = name;
                        existDevice.leftTime = TimeUnit.MINUTES.toMillis(leftTime);
                        existDevice.sourceId = sourceId;
                        existDevice.vipType = vipType;
                        new Client(mUniSDKInstance.getContext(), existDevice, ClientController.getExistClientController(uuid));
                        data.put(CODE, CODE_SUCCESS);
                        data.put(MSG, "connectCloudPhone success leftTime=" + leftTime + " minutes");
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "connectCloudPhone exception" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }
    }

    @UniJSMethod(uiThread = true)
    public void showToast(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            if (params == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "showToast params null");
            } else {
                String text = params.getString("text");
                if (TextUtils.isEmpty(text)) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "showToast text param empty");
                } else {
                    ToastUtils.showToastNoRepeat(text);
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "showToast success");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "showToast exception" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }
    }
}
