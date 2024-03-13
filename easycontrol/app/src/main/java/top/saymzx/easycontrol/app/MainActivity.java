package top.saymzx.easycontrol.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

import top.saymzx.easycontrol.app.databinding.ActivityMainBinding;
import top.saymzx.easycontrol.app.databinding.ItemRequestPermissionBinding;
import top.saymzx.easycontrol.app.databinding.ItemScanAddressListBinding;
import top.saymzx.easycontrol.app.databinding.ItemTextBinding;
import top.saymzx.easycontrol.app.entity.AppData;
import top.saymzx.easycontrol.app.helper.DeviceListAdapter;
import top.saymzx.easycontrol.app.helper.MyBroadcastReceiver;
import top.saymzx.easycontrol.app.helper.PublicTools;
import top.saymzx.easycontrol.app.helper.ViewTools;

public class MainActivity extends Activity {

  private ActivityMainBinding activityMainBinding;

  // 广播
  private final MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    AppData.init(this);
    ViewTools.setStatusAndNavBar(this);
    ViewTools.setLocale(this);
    activityMainBinding = ActivityMainBinding.inflate(this.getLayoutInflater());
    setContentView(activityMainBinding.getRoot());
    // 检测权限
    if (!checkPermission()) createAlert();
    else startApp();
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    myBroadcastReceiver.unRegister(this);
    super.onDestroy();
  }

  // 启动步骤
  private void startApp() {
    // 检测激活
//    checkActive();
    // 设置设备列表适配器
    DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this);
    activityMainBinding.devicesList.setAdapter(deviceListAdapter);
    myBroadcastReceiver.setDeviceListAdapter(deviceListAdapter);
    // 设置按钮监听
    setButtonListener();
    // 注册广播监听
    myBroadcastReceiver.register(this);
    // 检查已连接设备
    myBroadcastReceiver.checkConnectedUsb(this);
    // 自启动设备
    AppData.uiHandler.postDelayed(deviceListAdapter::startDefaultDevice, 1000);
  }

  // 检测激活
  private void checkActive() {
    if (!AppData.setting.getIsActive()) startActivity(new Intent(this, ActiveActivity.class));
  }

  // 检查权限
  private boolean checkPermission() {
//     检查悬浮窗权限，防止某些设备如鸿蒙不兼容
//    try {
//      return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
//    } catch (Exception ignored) {
//      return true;
//    }
    return true;
  }

  // 创建Client加载框
  private void createAlert() {
    ItemRequestPermissionBinding requestPermissionView = ItemRequestPermissionBinding.inflate(LayoutInflater.from(this));
    requestPermissionView.buttonGoToSet.setOnClickListener(v -> {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:$packageName"));
        startActivity(intent);
      }
    });
    Dialog dialog = ViewTools.createDialog(this, false, requestPermissionView.getRoot());
    dialog.show();
    checkPermissionDelay(dialog);
  }

  // 定时检查
  private void checkPermissionDelay(Dialog dialog) {
    // 因为某些设备可能会无法进入设置或其他问题，导致不会有返回结果，为了减少不确定性，使用定时检测的方法
    AppData.uiHandler.postDelayed(() -> {
      if (checkPermission()) {
        dialog.cancel();
        startApp();
      } else checkPermissionDelay(dialog);
    }, 1000);
  }

  // 设置按钮监听
  private void setButtonListener() {
    activityMainBinding.buttonScan.setOnClickListener(v -> scanAddress());
    activityMainBinding.buttonAdd.setOnClickListener(v -> startActivity(new Intent(this, DeviceDetailActivity.class)));
    activityMainBinding.buttonSet.setOnClickListener(v -> startActivity(new Intent(this, SetActivity.class)));
  }

  // 扫描局域网地址
  private void scanAddress() {
    Pair<View, WindowManager.LayoutParams> loading = ViewTools.createConnectLoading(this,false);
    AppData.windowManager.addView(loading.first, loading.second);
    new Thread(() -> {
      ArrayList<String> scannedAddresses = PublicTools.scanAddress();
      AppData.windowManager.removeView(loading.first);
      AppData.uiHandler.post(() -> {
        ItemScanAddressListBinding scanAddressListView = ItemScanAddressListBinding.inflate(LayoutInflater.from(this));
        Dialog dialog = ViewTools.createDialog(this, true, scanAddressListView.getRoot());
        for (String i : scannedAddresses) {
          ItemTextBinding text = ViewTools.createTextCard(this, i, () -> {
            AppData.clipBoard.setPrimaryClip(ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, i));
            Toast.makeText(this, getString(R.string.toast_copy), Toast.LENGTH_SHORT).show();
          });
          scanAddressListView.list.addView(text.getRoot());
        }
        dialog.show();
      });
    }).start();
  }

}