package com.feihu.cp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.alibaba.fastjson.JSONObject;
import com.feihu.cp.client.Client;
import com.feihu.cp.entity.AppData;
import com.feihu.cp.entity.Device;
import com.feihu.cp.helper.PublicTools;


import java.util.UUID;

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
        Log.d("test","testContext");
        JSONObject data = new JSONObject();
        try {
            if(mUniSDKInstance == null) {
                data.put("code", "fail");
                data.put("msg", "mUniSDKInstance null");
                Log.d("test","mUniSDKInstance null");
            } else {
                Context context = mUniSDKInstance.getContext();
                if(context == null) {
                    data.put("code", "fail");
                    data.put("msg", "mUniSDKInstance context null");
                    Log.d("test","mUniSDKInstance context null");
                } else {
                    data.put("code", "success");
                    data.put("msg", "mUniSDKInstance context not null");
                    Log.d("test","mUniSDKInstance context not null");
                }
            }

        }catch (Exception e) {
            try {
                data.put("code", "fail");
                data.put("msg", "mUniSDKInstance exception"+e.getMessage());
            }catch (Exception exception) {
                exception.printStackTrace();
            }
            Log.d("test","mUniSDKInstance exception");
            e.printStackTrace();
        }
        callback.invoke(data);

    }

    @UniJSMethod(uiThread = true)
    public void showToast(UniJSCallback callback) {
        Log.d("test","showToast");
        try {
            JSONObject data = new JSONObject();
            data.put("code", "success");
            data.put("msg", "call success");
            Log.d("test","showToast success");
            callback.invoke(data);
        }catch (Exception e) {
            e.printStackTrace();
            Log.d("test","showToast exception");
        }

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
                data.put(CODE, CODE_SUCCESS);
                data.put(MSG, "initModule success");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "initModule exception:"+e.getMessage());
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
                data.put(MSG, "address param null");
            } else {
                String address = params.getString("address");
                if (TextUtils.isEmpty(address)) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "address param empty");
                } else {
                    Pair<String, Integer> pair = null;
                    try {
                        pair = PublicTools.getIpAndPort(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (pair == null) {
                        data.put(CODE, CODE_FAIL);
                        data.put(MSG, "address param error");
                    } else {
                        Device device = new Device(UUID.randomUUID().toString(), Device.TYPE_NETWORK);
                        device.address = address;
                        new Client(mUniSDKInstance.getContext(),device, null);
                        data.put(CODE, CODE_SUCCESS);
                        data.put(MSG, "connecting phone");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "connecting phone exception"+e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }

    }
}
