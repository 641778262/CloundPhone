package top.saymzx.easycontrol.app.client;

import android.hardware.usb.UsbDevice;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;

import top.saymzx.easycontrol.app.entity.AppData;
import top.saymzx.easycontrol.app.entity.Device;
import top.saymzx.easycontrol.app.helper.ViewTools;

public class Client {
  // 组件
  private ClientStream clientStream = null;
  private ClientController clientController = null;
  private ClientPlayer clientPlayer = null;
  private final Device device;

  public Client(Device device) {
    this(device, null);
  }

  public Client(Device device, UsbDevice usbDevice) {
    this.device = device;
    Pair<View, WindowManager.LayoutParams> loading = ViewTools.createLoading(AppData.applicationContext);
    AppData.windowManager.addView(loading.first, loading.second);
    // 连接
    clientStream = new ClientStream(device, usbDevice, bool -> {
      if (bool) {
        // 控制器
        clientController = new ClientController(device, clientStream, str -> {
          if (str) {
            // 播放器
            clientPlayer = new ClientPlayer(device, clientStream, clientController);
          } else release();
        });
      }
      AppData.windowManager.removeView(loading.first);
    });
  }

  public void release() {
    AppData.dbHelper.update(device);
    clientPlayer.close();
    clientStream.close();
  }

}
