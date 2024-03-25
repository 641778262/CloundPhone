package com.feihu.cp.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.feihu.cp.R;
import com.feihu.cp.entity.AppData;
import com.feihu.cp.entity.Device;
import com.feihu.cp.helper.AppSettings;
import com.feihu.cp.helper.DeviceTools;
import com.feihu.cp.helper.ToastUtils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;


public class Client {
    // 组件
    private ClientStream clientStream = null;
    private ClientController clientController = null;
    private ClientPlayer clientPlayer = null;
    private final Device device;

    private static SoftReference<Dialog> dialogReference;

    public static void showDialog(Context context, Device device, ClientController existClientController) {
        if (!(context instanceof Activity) || device == null || TextUtils.isEmpty(device.address)) {
            return;
        }
        if(device.connectType != Device.CONNECT_TYPE_AUTO_CONNECT) {
            try {
                Dialog dialog = new Dialog(context, R.style.CustomDialog);
                dialogReference = new SoftReference<>(dialog);
                View contentView = View.inflate(context, R.layout.item_loading, null);
                TextView messageTv = contentView.findViewById(R.id.tv_message);

                switch (device.connectType) {
                    case Device.CONNECT_TYPE_RECONNECT:
                        messageTv.setText(R.string.connect_retry);
                        break;
                    case Device.CONNECT_TYPE_CHANGE_RESOLUTION:
                        messageTv.setText(R.string.connect_resolution);
                        break;
                    case Device.CONNECT_TYPE_CHANGE_NETWORK:
                        messageTv.setText(R.string.connect_change_network);
                        break;
                }
                dialog.setContentView(contentView);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (AppSettings.sUniApp) {
            Map<String, Object> params = new HashMap<>();
            params.put("uuid", device.uuid);
            params.put("address", device.address);
            params.put("name", device.name);
            DeviceTools.fireGlobalEvent("openPort", params);
        } else {
            new Client(context, device, existClientController);
        }

    }

    public static void dismissDialog() {
        try {
            if (dialogReference != null) {
                Dialog dialog = dialogReference.get();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialogReference.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Client(Context context, Device device, ClientController existClientController) {
        this(context, device, null, existClientController);
    }

    public Client(Context context, Device device, UsbDevice usbDevice, ClientController existClientController) {
        this.device = device;
        // 已经存在设备连接
        if (ClientController.getDevice(device.uuid) != null && existClientController == null) {
            dismissDialog();
            return;
        }
//        boolean retry = existClientController != null && !existClientController.autoReConnect;//主动点提示框重新连接
//    Context context = AppData.applicationContext;
        if (existClientController != null && existClientController.fullView != null) {
            context = existClientController.fullView;
        }

//    Pair<View, WindowManager.LayoutParams> loading = null;
//    if(existClientController == null || !existClientController.autoReConnect){//第一次连接或者非自动连接
//      loading = ViewTools.createConnectLoading(context,retry);
//      AppData.windowManager.addView(loading.first, loading.second);
//    }
//    final Pair<View, WindowManager.LayoutParams> loadingPair = loading;

        final Context copyContext = context;
        // 连接
        clientStream = new ClientStream(device, usbDevice, connected -> {
            if (existClientController != null) {
                existClientController.handleException = false;
            }
            dismissDialog();
            AppSettings.sConnected = connected;
            if (connected) {//连接成功
                if(device.connectType == Device.CONNECT_TYPE_CHANGE_RESOLUTION) {
                    switch (AppSettings.getResolutionType()) {
                        case AppSettings.RESOLUTION_SUPER:
                            ToastUtils.showToastNoRepeat(R.string.change_resolution_super);
                            break;
                        case AppSettings.RESOLUTION_HIGH:
                            ToastUtils.showToastNoRepeat(R.string.change_resolution_high);
                            break;
                        default:
                            ToastUtils.showToastNoRepeat(R.string.change_resolution_common);
                            break;
                    }

                } else if(device.connectType == Device.CONNECT_TYPE_CHANGE_NETWORK) {
                    if(DeviceTools.isWiFiNet()) {
                        ToastUtils.showToastNoRepeat(R.string.connect_wifi_tips);
                    } else if(DeviceTools.isMobileNet()) {
                        ToastUtils.showToastNoRepeat(R.string.connect_mobile_tips);
                    }
                } else if(device.connectType != Device.CONNECT_TYPE_NORMAL){
                    if(DeviceTools.isMobileNet()) {
                        ToastUtils.showToastNoRepeat(R.string.connect_mobile_tips);
                    }
                }
                AppSettings.resetLastTouchTime();
                // 控制器
                if (existClientController == null) {
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
                    clientController.setClientStream(clientStream, ready -> {
                        if (ready) {//TextureView准备就绪可以播放
                            // 播放器
                            clientPlayer = new ClientPlayer(device, clientStream, clientController);
                        } else {//退出连接界面或者重连时主动释放上一次连接资源
                            release();
                        }
                    });
                }
            } else {
                if (existClientController != null) {//处理失败情况
                    ClientController.showConnectDialog(existClientController);
                }
            }

        });
    }

    public void release() {
        AppSettings.sConnected = false;
        AppData.dbHelper.update(device);
        if (clientPlayer != null) {
            clientPlayer.close();
            clientPlayer = null;
        }
        if (clientStream != null) {
            clientStream.close();
            clientStream = null;
        }
    }

}
