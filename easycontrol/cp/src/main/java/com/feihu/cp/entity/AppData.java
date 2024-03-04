package com.feihu.cp.entity;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.view.WindowManager;

import com.feihu.cp.MainActivity;
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
  public static ClipboardManager clipBoard;
  public static WifiManager wifiManager;
  public static UsbManager usbManager;
  public static WindowManager windowManager;
  public static SensorManager sensorManager;

  // 设置值
  public static Setting setting;

  public static void init(Context context) {
    applicationContext = context.getApplicationContext();
    AppSettings.initAppSettings();
    uiHandler = new Handler(context.getMainLooper());
    dbHelper = new DbHelper(applicationContext);
    clipBoard = (ClipboardManager) applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
    wifiManager = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
    usbManager = (UsbManager) applicationContext.getSystemService(Context.USB_SERVICE);
    windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
    sensorManager = (SensorManager) applicationContext.getSystemService(Context.SENSOR_SERVICE);
    setting = new Setting(applicationContext.getSharedPreferences("setting", Context.MODE_PRIVATE));
    // 读取密钥
    keyPair = PublicTools.readAdbKeyPair();
  }

}
