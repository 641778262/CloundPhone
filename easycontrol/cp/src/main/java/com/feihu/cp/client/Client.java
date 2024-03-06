package com.feihu.cp.client;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;

import com.feihu.cp.entity.AppData;
import com.feihu.cp.entity.Device;
import com.feihu.cp.helper.AppSettings;
import com.feihu.cp.helper.ViewTools;

public class Client {
  // 组件
  private ClientStream clientStream = null;
  private ClientController clientController = null;
  private ClientPlayer clientPlayer = null;
  private final Device device;

  public Client(Device device,ClientController existClientController) {
    this(device, null,existClientController);
  }

  public Client(Device device, UsbDevice usbDevice,ClientController existClientController) {
    this.device = device;
    // 已经存在设备连接
    if (ClientController.getDevice(device.uuid) != null && existClientController == null) return;
    boolean retry = existClientController != null && !existClientController.autoReConnect;//主动点提示框重新连接
    Context context = AppData.applicationContext;
    if(existClientController != null && existClientController.fullView != null) {
      context = existClientController.fullView;
    }
//    Pair<View, WindowManager.LayoutParams> loading = null;
//    if(existClientController == null || !existClientController.autoReConnect){//第一次连接或者非自动连接
//      loading = ViewTools.createConnectLoading(context,retry);
//      AppData.windowManager.addView(loading.first, loading.second);
//    }
//    final Pair<View, WindowManager.LayoutParams> loadingPair = loading;

    // 连接
    clientStream = new ClientStream(device, usbDevice, connected -> {
      if(existClientController != null) {
        existClientController.handleException = false;
      }
      try {
//        if(loadingPair != null) {
//          AppData.windowManager.removeView(loadingPair.first);
//        }
      } catch (Exception ignored) {
        ignored.printStackTrace();
      }
      AppSettings.sConnected = connected;
      AppData.uiHandler.post(() -> {

      });
      if (connected) {//连接成功
        // 控制器
        if(existClientController == null) {
          clientController = new ClientController(device, clientStream, ready -> {
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
