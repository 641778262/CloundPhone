package com.feihu.cp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.fastjson.JSONObject;
import com.feihu.cp.client.Client;
import com.feihu.cp.client.ClientController;
import com.feihu.cp.entity.AppData;
import com.feihu.cp.entity.Device;
import com.feihu.cp.file.MediaStoreHelper;
import com.feihu.cp.helper.AppSettings;
import com.feihu.cp.helper.DeviceTools;
import com.feihu.cp.helper.ToastUtils;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
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
                    AppSettings.setControlMode(fullScreen ? AppSettings.CONTROL_MODE_DEFAULT : AppSettings.CONTROL_MODE_PROFESSIONAL);
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
                String system = params.getString("system");
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
                        existDevice.system = system;
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

    @UniJSMethod(uiThread = true)
    public void getAppInfoListByPath(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            com.alibaba.fastjson.JSONArray paths;
            if (params == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "getAppInfoListByPath params null");
                if (callback != null) {
                    callback.invoke(data);
                }
            } else {
                paths = params.getJSONArray("paths");
//                if (object instanceof String[]) {
//                    paths = (String[]) params.get("paths");
//                } else {
//                    data.put(CODE, CODE_FAIL);
//                    data.put(MSG, "getAppInfoListByPath paths class not match:"+object.getClass().toString());
//                    if (callback != null) {
//                        callback.invoke(data);
//                    }
//                    return;
//                }
                if (paths == null || paths.size() == 0) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "getAppInfoListByPath paths empty");
                    if (callback != null) {
                        callback.invoke(data);
                    }
                } else {
                    Executors.newCachedThreadPool().execute(() -> {
                        try {
                            int quality = params.getIntValue("quality");
                            if (quality == 0) {
                                quality = 50;
                            }
                            JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < paths.size(); i++) {
                                try {
                                    String path = paths.getString(i);
                                    if (TextUtils.isEmpty(path)) {
                                        continue;
                                    }
                                    File file = new File(path);
                                    if (!file.exists() || !file.isFile()) {
                                        continue;
                                    }

                                    PackageManager pm = AppData.applicationContext.getPackageManager();
                                    PackageInfo info = pm.getPackageArchiveInfo(path,
                                            PackageManager.GET_ACTIVITIES);
                                    if (info != null) {
                                        ApplicationInfo appInfo = info.applicationInfo;
                                        appInfo.sourceDir = path;
                                        appInfo.publicSourceDir = path;
                                        Drawable drawable = appInfo.loadIcon(pm);
                                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                                        String str = bitmap2Base64(bitmap, quality, Bitmap.CompressFormat.PNG);
                                        org.json.JSONObject jsonObject = new org.json.JSONObject();
                                        jsonObject.put("icon", "data:image/png;base64," + str);
                                        jsonObject.put("appName", pm.getApplicationLabel(appInfo).toString());
                                        jsonObject.put("packageName", info.packageName);
                                        jsonObject.put("versionName", info.versionName);
                                        jsonObject.put("versionCode", info.versionCode);
                                        jsonObject.put("size", file.length());
                                        jsonObject.put("path", path);
                                        PackageInfo packageInfo = AppData.applicationContext.getPackageManager().getPackageInfo(info.packageName, 0);
                                        jsonObject.put("isInstalled", packageInfo != null);
                                        jsonArray.put(jsonObject);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                            if (jsonArray.length() == 0) {
                                data.put(CODE, CODE_FAIL);
                                data.put(MSG, "getAppInfoListByPath no valid path");
                            } else {
                                data.put(CODE, CODE_SUCCESS);
                                data.put(MSG, "getAppInfoListByPath success");
                                data.put("array", jsonArray.toString());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            data.put(CODE, CODE_FAIL);
                            data.put(MSG, "getAppInfoListByPath exception" + e.getMessage());
                        }
                        if (callback != null) {
                            callback.invoke(data);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            data.put(CODE, CODE_FAIL);
            data.put(MSG, "getAppInfoListByPath exception" + e.getMessage());
            if (callback != null) {
                callback.invoke(data);
            }
        }

    }

    @UniJSMethod(uiThread = true)
    public void getAppInfoByPath(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            if (params == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "getAppInfoByPath params null");
            } else {
                String path = params.getString("path");
                int quality = params.getIntValue("quality");
                if (quality == 0) {
                    quality = 50;
                }
                if (TextUtils.isEmpty(path)) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "getAppInfoByPath path param empty");
                } else {
                    File file = new File(path);
                    if (!file.exists() || !file.isFile()) {
                        data.put(CODE, CODE_FAIL);
                        data.put(MSG, "getAppInfoByPath file not exists or not a file");
                    } else {
                        PackageManager pm = AppData.applicationContext.getPackageManager();
                        PackageInfo info = pm.getPackageArchiveInfo(path,
                                PackageManager.GET_ACTIVITIES);
                        if (info != null) {
                            ApplicationInfo appInfo = info.applicationInfo;
                            appInfo.sourceDir = path;
                            appInfo.publicSourceDir = path;
                            Drawable drawable = appInfo.loadIcon(pm);
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            String str = bitmap2Base64(bitmap, quality, Bitmap.CompressFormat.PNG);
                            data.put(CODE, CODE_SUCCESS);
                            data.put(MSG, "getAppInfoByPath success");
                            data.put("icon", "data:image/png;base64," + str);
                            data.put("appName", pm.getApplicationLabel(appInfo).toString());
                            data.put("packageName", info.packageName);
                            data.put("versionName", info.versionName);
                            data.put("versionCode", info.versionCode);
                            data.put("size", file.length());
                            data.put("path", path);
                            try {
                                PackageInfo packageInfo = AppData.applicationContext.getPackageManager().getPackageInfo(info.packageName, 0);
                                data.put("isInstalled", packageInfo != null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "getAppInfoByPath exception" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }
    }

    private static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static String bitmap2Base64(Bitmap bitmap, int compress, Bitmap.CompressFormat format) {
        if (bitmap == null) {
            return null;
        }

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(format != null ? format : Bitmap.CompressFormat.JPEG, compress, baos);

            baos.flush();
            baos.close();

            byte[] bitmapBytes = baos.toByteArray();
            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

//    @UniJSMethod(uiThread = true)
//    public void readFileByPath(JSONObject params, UniJSCallback callback) {
//        JSONObject data = new JSONObject();
//        try {
//            if (params == null) {
//                data.put(CODE, CODE_FAIL);
//                data.put(MSG, "getAppInfoByPath params null");
//            } else {
//                String path = params.getString("path");
//                int quality = params.getIntValue("quality");
//                if (quality == 0) {
//                    quality = 50;
//                }
//                if (TextUtils.isEmpty(path)) {
//                    data.put(CODE, CODE_FAIL);
//                    data.put(MSG, "getAppInfoByPath path param empty");
//                } else {
//                    File file = new File(path);
//                    if (!file.exists() || !file.isFile()) {
//                        data.put(CODE, CODE_FAIL);
//                        data.put(MSG, "getAppInfoByPath file not exists or not a file");
//                    } else {
//                        PackageManager pm = AppData.applicationContext.getPackageManager();
//                        PackageInfo info = pm.getPackageArchiveInfo(path,
//                                PackageManager.GET_ACTIVITIES);
//                        if (info != null) {
//                            ApplicationInfo appInfo = info.applicationInfo;
//                            appInfo.sourceDir = path;
//                            appInfo.publicSourceDir = path;
//                            Drawable drawable = appInfo.loadIcon(pm);
//                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//                            String str = bitmap2Base64(bitmap, quality, Bitmap.CompressFormat.PNG);
//                            data.put(CODE, CODE_SUCCESS);
//                            data.put(MSG, "getAppInfoByPath success");
//                            data.put("icon", "data:image/png;base64," + str);
//                            data.put("name", pm.getApplicationLabel(appInfo).toString());
//                            data.put("pn", info.packageName);
//                            data.put("vn", info.versionName);
//                            data.put("vc", info.versionCode);
//                            data.put("size", file.length());
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            try {
//                data.put(CODE, CODE_FAIL);
//                data.put(MSG, "getAppInfoByPath exception" + e.getMessage());
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
//        }
//        if (callback != null) {
//            callback.invoke(data);
//        }
//    }

    @UniJSMethod(uiThread = true)
    public void readFileToString(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            if (params == null) {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "readFileToString params null");
                if (callback != null) {
                    callback.invoke(data);
                }
            } else {
                String path = params.getString("path");
                long start = params.getLong("start");
                long end = params.getLong("end");
                if (TextUtils.isEmpty(path) || start < 0 || end <= 0) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "readFileToString params invalid");
                    if (callback != null) {
                        callback.invoke(data);
                    }
                } else {
                    File file = new File(path);
                    if (!file.exists() || !file.isFile()) {
                        data.put(CODE, CODE_FAIL);
                        data.put(MSG, "readFileToString file not exists or not a file");
                        if (callback != null) {
                            callback.invoke(data);
                        }
                    } else {
                        Executors.newCachedThreadPool().execute(() -> {
                            FileInputStream fis = null;
                            try {
                                fis = new FileInputStream(file);
                                int capacity = (int) (end - start + 1);
                                fis.skip(start);
                                byte[] buffer = new byte[capacity];
                                fis.read(buffer, 0, buffer.length);
                                String str = Base64.encodeToString(buffer, Base64.NO_WRAP);
                                data.put(CODE, CODE_SUCCESS);
                                data.put(MSG, "readFileToString success");
                                data.put("str", str);
                                if (callback != null) {
                                    callback.invoke(data);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    data.put(CODE, CODE_FAIL);
                                    data.put(MSG, "readFileToString exception" + e.getMessage());
                                    if (callback != null) {
                                        callback.invoke(data);
                                    }
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            } finally {
                                try {
                                    if (fis != null) {
                                        fis.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "readFileToString exception" + e.getMessage());
                if (callback != null) {
                    callback.invoke(data);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @UniJSMethod(uiThread = true)
    public void searchFiles(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        if (params == null || TextUtils.isEmpty(params.getString("keyWords"))) {
            data.put(CODE, CODE_FAIL);
            data.put(MSG, "searchFiles params null or keyWords null");
            if (callback != null) {
                callback.invoke(data);
            }
            return;
        }
        String keyWords = params.getString("keyWords");
        try {
            FragmentActivity activity = (FragmentActivity) mUniSDKInstance.getContext();
            MediaStoreHelper.getAllBookFile(activity, keyWords, (MediaStoreHelper.MediaResultCallback) files -> {
                if (files == null || files.size() == 0) {
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "searchFiles file empty");
                    if (callback != null) {
                        callback.invoke(data);
                    }
                } else {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        org.json.JSONObject jsonObject = new org.json.JSONObject();
                        try {
                            String path = file.getAbsolutePath();
                            jsonObject.put("filePath", path);
                            jsonObject.put("fileSize", file.length());
                            int index = path.lastIndexOf("/");
                            String fileName = path.substring(index + 1);
                            jsonObject.put("fileName", fileName);
                            if (fileName.contains(".")) {
                                int typeIndex = fileName.lastIndexOf(".");
                                String type = fileName.substring(typeIndex + 1);
                                jsonObject.put("fileType", type);
                            } else {
                                jsonObject.put("fileType", fileName);
                            }
                            jsonArray.put(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "searchFiles success");
                    data.put("str", jsonArray.toString());
                    if (callback != null) {
                        callback.invoke(data);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "searchFiles exception" + e.getMessage());
                if (callback != null) {
                    callback.invoke(data);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    @UniJSMethod(uiThread = true)
    public void getAppList(JSONObject params, UniJSCallback callback) {
        int quality = 0;
        if (params != null) {
            quality = params.getIntValue("quality");
        }
        if (quality == 0) {
            quality = 50;
        }
        final int copy = quality;
        JSONObject data = new JSONObject();
        try {
            Executors.newCachedThreadPool().execute(() -> {
                try {
                    PackageManager packageManager = AppData.applicationContext.getPackageManager();
                    //获取到所有的安装包
                    List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
                    JSONArray jsonArray = new JSONArray();
                    for (PackageInfo installedPackage : installedPackages) {
                        //判断当前是否是系统app
                        try {
                            if ((installedPackage.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                continue;
                            }
                            //程序包名
                            org.json.JSONObject jsonObject = new org.json.JSONObject();
                            String appName = installedPackage.applicationInfo.loadLabel(packageManager).toString();
                            jsonObject.put("name", appName);
                            jsonObject.put("package", installedPackage.packageName);
                            jsonObject.put("version", installedPackage.versionName);
                            jsonObject.put("versionCode", installedPackage.packageName);
                            Drawable icon = installedPackage.applicationInfo.loadIcon(packageManager);
                            Bitmap bitmap = getBitmapFromDrawable(icon);
                            String str = bitmap2Base64(bitmap, copy, android.graphics.Bitmap.CompressFormat.PNG);
                            jsonObject.put("icon", "data:image/png;base64," + str);
                            String sourceDir = installedPackage.applicationInfo.sourceDir;
                            jsonObject.put("path", sourceDir);
                            File file = new File(sourceDir);
                            jsonObject.put("size", file.length());
                            jsonArray.put(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "getAppList success");
                    data.put("str", jsonArray.toString());
                    if (callback != null) {
                        callback.invoke(data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        data.put(CODE, CODE_FAIL);
                        data.put(MSG, "getAppList exception" + e.getMessage());
                        if (callback != null) {
                            callback.invoke(data);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "getAppList exception" + e.getMessage());
                if (callback != null) {
                    callback.invoke(data);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @UniJSMethod(uiThread = true)
    public void getSDCardRootPath(JSONObject params, UniJSCallback callback) {
        JSONObject data = new JSONObject();
        try {
            boolean sdcardExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            if (sdcardExists) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                if (TextUtils.isEmpty(path)) {
                    data.put(CODE, CODE_FAIL);
                    data.put(MSG, "getSDCardRootPath path empty");
                } else {
                    data.put(CODE, CODE_SUCCESS);
                    data.put(MSG, "getSDCardRootPath success");
                    data.put("path", path);
                }
            } else {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "getSDCardRootPath sdCard not exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                data.put(CODE, CODE_FAIL);
                data.put(MSG, "getSDCardRootPath exception" + e.getMessage());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (callback != null) {
            callback.invoke(data);
        }
    }

}
