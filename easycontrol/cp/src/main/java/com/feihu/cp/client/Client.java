package com.feihu.cp.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.View;

import com.feihu.cp.R;
import com.feihu.cp.entity.AppData;
import com.feihu.cp.entity.Device;
import com.feihu.cp.helper.AppSettings;


public class Client {
  // 组件
  private ClientStream clientStream = null;
  private ClientController clientController = null;
  private ClientPlayer clientPlayer = null;
  private final Device device;

  public Client(Context context,Device device,ClientController existClientController) {
    this(context,device, null,existClientController);
  }

  public Client(Context context, Device device, UsbDevice usbDevice, ClientController existClientController) {
    this.device = device;
    // 已经存在设备连接
    if (ClientController.getDevice(device.uuid) != null && existClientController == null) return;
    boolean retry = existClientController != null && !existClientController.autoReConnect;//主动点提示框重新连接
//    Context context = AppData.applicationContext;
    if(existClientController != null && existClientController.fullView != null) {
      context = existClientController.fullView;
    }

//    Pair<View, WindowManager.LayoutParams> loading = null;
//    if(existClientController == null || !existClientController.autoReConnect){//第一次连接或者非自动连接
//      loading = ViewTools.createConnectLoading(context,retry);
//      AppData.windowManager.addView(loading.first, loading.second);
//    }
//    final Pair<View, WindowManager.LayoutParams> loadingPair = loading;

    if(!(context instanceof Activity)) {
      return;
    }
    Dialog dialog = null;
    try {
      dialog = new Dialog(context, R.style.CustomDialog);
      dialog.setContentView(View.inflate(context,R.layout.item_loading,null));
      dialog.setCancelable(false);
      dialog.setCanceledOnTouchOutside(false);
      dialog.show();
    }catch (Exception e) {
      e.printStackTrace();
    }
    final Dialog loadingDialog = dialog;
    final Context copyContext = context;
    // 连接
    clientStream = new ClientStream(device, usbDevice, connected -> {
      if(existClientController != null) {
        existClientController.handleException = false;
      }
      try {
        if(loadingDialog != null && loadingDialog.isShowing()) {
          loadingDialog.dismiss();
        }
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }
      AppSettings.sConnected = connected;
      AppData.uiHandler.post(() -> {

      });
      if (connected) {//连接成功
        AppSettings.resetLastTouchTime();
        // 控制器
        if(existClientController == null) {
          clientController = new ClientController(copyContext, device, clientStream, ready -> {
            if (ready) {//TextureView准备就绪可以播放
              // 播放器
              clientPlayer = new ClientPlayer(device, clientStream, clientController);
            } else {//退出连接界面或者重连时主动释放上一次连接资源
              release();
            }
          });
        } else {
          clientController = existClientController;
          clientController.setClientStream(clientStream,ready -> {
            if (ready) {//TextureView准备就绪可以播放
              // 播放器
              clientPlayer = new ClientPlayer(device, clientStream, clientController);
            } else {//退出连接界面或者重连时主动释放上一次连接资源
              release();
            }
          });
        }
      } else {
        if(existClientController != null) {//处理失败情况
          ClientController.showConnectDialog(existClientController);
        }
      }

    });
  }

  public void release() {
    AppSettings.sConnected = false;
    AppData.dbHelper.update(device);
    if(clientPlayer != null) {
      clientPlayer.close();
      clientPlayer = null;
    }
    if(clientStream != null) {
      clientStream.close();
      clientStream = null;
    }
  }

}
