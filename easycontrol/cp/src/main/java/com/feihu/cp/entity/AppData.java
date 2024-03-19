package com.feihu.cp.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.feihu.cp.adb.AdbKeyPair;
import com.feihu.cp.helper.AppSettings;
import com.feihu.cp.helper.DbHelper;
import com.feihu.cp.helper.PublicTools;

public class AppData {
    @SuppressLint("StaticFieldLeak")
    public static Context applicationContext;
    public static Handler uiHandler;

    // 数据库工具库
    public static DbHelper dbHelper;
    // 密钥文件
    public static AdbKeyPair keyPair;
    // 系统服务

    public static void init(Context context) {
        if (applicationContext != null) {
            return;
        }
        applicationContext = context.getApplicationContext();
        AppSettings.initAppSettings();
        uiHandler = new Handler(context.getMainLooper());
        dbHelper = new DbHelper(applicationContext);
        // 读取密钥
        keyPair = PublicTools.readAdbKeyPair();
    }

}
