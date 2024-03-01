package top.saymzx.easycontrol.app.client.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;

import top.saymzx.easycontrol.app.R;
import top.saymzx.easycontrol.app.client.ClientController;
import top.saymzx.easycontrol.app.client.ControlPacket;
import top.saymzx.easycontrol.app.databinding.ActivityFullBinding;
import top.saymzx.easycontrol.app.entity.AppData;
import top.saymzx.easycontrol.app.entity.Device;
import top.saymzx.easycontrol.app.helper.AppSettings;
import top.saymzx.easycontrol.app.helper.CustomOnClickListener;
import top.saymzx.easycontrol.app.helper.DeviceTools;
import top.saymzx.easycontrol.app.helper.PublicTools;
import top.saymzx.easycontrol.app.helper.ViewTools;

public class FullActivity extends Activity implements SensorEventListener {
  private boolean isClose = false;
  private Device device;
  private ActivityFullBinding activityFullBinding;
  private boolean autoRotate;
  private boolean light = true;

  private long lastBackPressTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewTools.setFullScreen(this);
    activityFullBinding = ActivityFullBinding.inflate(this.getLayoutInflater());
    setContentView(activityFullBinding.getRoot());
    device = ClientController.getDevice(getIntent().getStringExtra("uuid"));
    if (device == null) return;
    ClientController.setFullView(device.uuid, this);
    // 初始化
    activityFullBinding.barView.setVisibility(View.GONE);
    setNavBarHide(AppSettings.showVirtualKeys());
    autoRotate = AppData.setting.getAutoRotate();
    activityFullBinding.buttonAutoRotate.setImageResource(autoRotate ? R.drawable.un_rotate : R.drawable.rotate);
    // 按键监听
    setButtonListener();
    setKeyEvent();
    // 更新textureView
    activityFullBinding.textureViewLayout.addView(ClientController.getTextureView(device.uuid), 0);
    activityFullBinding.textureViewLayout.post(this::updateMaxSize);
    // 页面自动旋转
    AppData.sensorManager.registerListener(this, AppData.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause() {
    AppData.sensorManager.unregisterListener(this);
    if (isChangingConfigurations()) activityFullBinding.textureViewLayout.removeView(ClientController.getTextureView(device.uuid));
//    else if (!isClose) ClientController.handleControll(device.uuid, device.fullToMiniOnRunning ? "changeToMini" : "changeToSmall", ByteBuffer.wrap("changeToFull".getBytes()));
    super.onPause();
  }

  @Override
  public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
    activityFullBinding.textureViewLayout.post(this::updateMaxSize);
    super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
  }

  @Override
  public void onBackPressed() {
  }

  private void updateMaxSize() {
    int width = activityFullBinding.textureViewLayout.getMeasuredWidth();
    int height = activityFullBinding.textureViewLayout.getMeasuredHeight();
    ByteBuffer byteBuffer = ByteBuffer.allocate(8);
    byteBuffer.putInt(width);
    byteBuffer.putInt(height);
    byteBuffer.flip();
    ClientController.handleControll(device.uuid, "updateMaxSize", byteBuffer);
    if (!device.customResolutionOnConnect && device.changeResolutionOnRunning) ClientController.handleControll(device.uuid, "writeByteBuffer", ControlPacket.createChangeResolutionEvent((float) width / height));
  }

  public void hide() {
    try {
      isClose = true;
      activityFullBinding.textureViewLayout.removeView(ClientController.getTextureView(device.uuid));
      finish();
    } catch (Exception ignored) {
    }
  }

  // 设置按钮监听
  private void setButtonListener() {
    activityFullBinding.buttonRotate.setOnClickListener(v -> ClientController.handleControll(device.uuid, "buttonRotate", null));
    activityFullBinding.buttonBack.setOnClickListener(v -> ClientController.handleControll(device.uuid, "buttonBack", null));
    activityFullBinding.buttonHome.setOnClickListener(v -> ClientController.handleControll(device.uuid, "buttonHome", null));
    activityFullBinding.buttonSwitch.setOnClickListener(v -> ClientController.handleControll(device.uuid, "buttonSwitch", null));
    activityFullBinding.buttonNavBar.setOnClickListener(v -> {
      setNavBarHide(activityFullBinding.navBar.getVisibility() == View.GONE);
      changeBarView();
    });
    activityFullBinding.buttonMini.setOnClickListener(v -> ClientController.handleControll(device.uuid, "changeToMini", null));
    activityFullBinding.buttonSmall.setOnClickListener(v -> ClientController.handleControll(device.uuid, "changeToSmall", null));
    activityFullBinding.buttonClose.setOnClickListener(v -> ClientController.handleControll(device.uuid, "close", null));
    activityFullBinding.buttonLight.setOnClickListener(v -> {
      light = !light;
      activityFullBinding.buttonLight.setImageResource(light ? R.drawable.lightbulb_off : R.drawable.lightbulb);
      ClientController.handleControll(device.uuid, light ? "buttonLight" : "buttonLightOff", null);
      changeBarView();
    });
    activityFullBinding.buttonPower.setOnClickListener(v -> {
      ClientController.handleControll(device.uuid, "buttonPower", null);
      changeBarView();
    });
    activityFullBinding.buttonMore.setOnClickListener(v -> changeBarView());
    activityFullBinding.buttonAutoRotate.setOnClickListener(v -> {
      autoRotate = !autoRotate;
      AppData.setting.setAutoRotate(autoRotate);
      activityFullBinding.buttonAutoRotate.setImageResource(autoRotate ? R.drawable.un_rotate : R.drawable.rotate);
      changeBarView();
    });
      activityFullBinding.buttonSetting.setOnClickListener(v -> {
          showPopupWindow();
      });
  }

  // 导航栏隐藏
  private void setNavBarHide(boolean isShow) {
    activityFullBinding.navBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    activityFullBinding.buttonNavBar.setImageResource(isShow ? R.drawable.not_equal : R.drawable.equals);
    activityFullBinding.textureViewLayout.post(this::updateMaxSize);
  }

  private void changeBarView() {
    boolean toShowView = activityFullBinding.barView.getVisibility() == View.GONE;
    boolean isLandscape = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    ViewTools.viewAnim(activityFullBinding.barView, toShowView, 0, PublicTools.dp2px(40f) * (isLandscape ? -1 : 1), (isStart -> {
      if (isStart && toShowView) activityFullBinding.barView.setVisibility(View.VISIBLE);
      else if (!isStart && !toShowView) activityFullBinding.barView.setVisibility(View.GONE);
    }));
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (!autoRotate || Sensor.TYPE_ACCELEROMETER != sensorEvent.sensor.getType()
            || activityFullBinding.textureViewLayout.getMeasuredWidth() < activityFullBinding.textureViewLayout.getMeasuredHeight()) return;
    float[] values = sensorEvent.values;
    float x = values[0];
    float y = values[1];
    int newOrientation = getRequestedOrientation();
    if (y > -3 && y < 3 && x >= 4.5) newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    else if (y > -3 && y < 3 && x <= -4.5) newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

    if (getRequestedOrientation() != newOrientation) {
      setRequestedOrientation(newOrientation);
    }

  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {

  }

  // 设置键盘监听
  private void setKeyEvent() {
//    activityFullBinding.editText.requestFocus();
//    activityFullBinding.editText.setInputType(InputType.TYPE_NULL);
//    activityFullBinding.editText.setOnKeyListener((v, keyCode, event) -> {
//      if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
//        ClientController.handleControll(device.uuid, "writeByteBuffer", ControlPacket.createKeyEvent(event.getKeyCode(), event.getMetaState()));
//        return true;
//      }
//      return false;
//    });
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    int action = event.getAction();

    if(action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
      if(System.currentTimeMillis() - lastBackPressTime < 2000) {
        ClientController.handleControll(device.uuid, "close", null);
      } else {
        lastBackPressTime = System.currentTimeMillis();
        Toast.makeText(getApplicationContext(),"再按一次退出云手机", Toast.LENGTH_SHORT).show();
      }
      return true;
    }
    if (action == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
      ClientController.handleControll(device.uuid, "writeByteBuffer", ControlPacket.createKeyEvent(event.getKeyCode(), event.getMetaState()));
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
    private PopupWindow showPopupWindow() {
        View view = View.inflate(this, R.layout.popup_setting, null);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        boolean isLandscape = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        int width = isLandscape ? DeviceTools.getScreenWidth() * 3 / 5 : DeviceTools.getScreenWidth() * 5 / 6;
        final PopupWindow popupWindow = new PopupWindow(view, width, view
                .getMeasuredHeight(), true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        TextView voiceTv = view.findViewById(R.id.tv_voice);
        voiceTv.setText(AppSettings.showVoice() ? R.string.device_voice_off : R.string.device_voice_on);
        TextView keyTv = view.findViewById(R.id.tv_key);
        keyTv.setText(AppSettings.showVirtualKeys() ? R.string.device_hide_keys : R.string.device_show_keys);
        CustomOnClickListener listener = new CustomOnClickListener() {
            @Override
            public void onClickView(View view) {
                switch (view.getId()) {
                    case R.id.tv_voice:
                        if(AppSettings.showVoice()) {
                            AppSettings.setShowVoice(false);
                            voiceTv.setText(R.string.device_voice_on);
                        } else {
                            AppSettings.setShowVoice(true);
                            voiceTv.setText(R.string.device_voice_off);
                        }
                        break;
                    case R.id.tv_key:
                        if(AppSettings.showVirtualKeys()) {
                            AppSettings.setShowVirtualKeys(false);
                            keyTv.setText(R.string.device_show_keys);
                            setNavBarHide(false);
                        } else {
                            AppSettings.setShowVirtualKeys(true);
                            keyTv.setText(R.string.device_hide_keys);
                            setNavBarHide(true);

                        }
                        break;
                    case R.id.tv_reboot:
                        break;
                    case R.id.tv_exit:
                        ClientController.handleControll(device.uuid, "close", null);
                        break;
                    case R.id.iv_home:
                        ClientController.handleControll(device.uuid, "buttonHome", null);
                        break;
                    case R.id.iv_switch:
                        ClientController.handleControll(device.uuid, "buttonSwitch", null);
                        break;
                    case R.id.iv_back:
                        ClientController.handleControll(device.uuid, "buttonBack", null);
                        break;
                }
            }
        };

        voiceTv.setOnClickListener(listener);
        keyTv.setOnClickListener(listener);
        view.findViewById(R.id.tv_reboot).setOnClickListener(listener);
        view.findViewById(R.id.tv_exit).setOnClickListener(listener);
        view.findViewById(R.id.iv_home).setOnClickListener(listener);
        view.findViewById(R.id.iv_switch).setOnClickListener(listener);
        view.findViewById(R.id.iv_back).setOnClickListener(listener);

        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        return popupWindow;
    }
}