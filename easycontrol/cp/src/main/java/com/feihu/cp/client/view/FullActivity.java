package com.feihu.cp.client.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.feihu.cp.R;
import com.feihu.cp.client.ClientController;
import com.feihu.cp.client.ControlPacket;
import com.feihu.cp.entity.Device;
import com.feihu.cp.helper.AppSettings;
import com.feihu.cp.helper.CustomOnClickListener;
import com.feihu.cp.helper.DeviceTools;
import com.feihu.cp.helper.PingUtils;
import com.feihu.cp.helper.StatusBarUtil;
import com.feihu.cp.helper.ToastUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class FullActivity extends Activity implements SensorEventListener {
    private boolean isClose = false;
    private Device device;
    private long lastBackPressTime;

    private ViewGroup textureViewLayout;
    private final PingUtils mPingUtils = new PingUtils();
    private SensorManager sensorManager;

    private long mInitTime;

    private static final int MSG_CHECK_TOUCH_TIME = 1001;
    private static final int MSG_CHECK_LEFT_TIME = 1002;
    private static final long CHECK_TOUCH_TIME_INTERVAL = 5 * 1000;
    private static final long CHECK_LEFT_TIME_INTERVAL = 5 * 1000;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_CHECK_TOUCH_TIME) {
                if (showNoHandleTimeOut()) {
                    removeCallbacksAndMessages(MSG_CHECK_TOUCH_TIME);
                } else {
                    sendEmptyMessageDelayed(MSG_CHECK_TOUCH_TIME, CHECK_TOUCH_TIME_INTERVAL);
                }
            } else if (msg.what == MSG_CHECK_LEFT_TIME) {
                if (showLeftTimeDialog()) {
                    removeCallbacksAndMessages(MSG_CHECK_LEFT_TIME);
                } else {
                    sendEmptyMessageDelayed(MSG_CHECK_LEFT_TIME, CHECK_LEFT_TIME_INTERVAL);
                }
            }
        }
    };

    private class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                int currentState = DeviceTools.getNetworkType();
                if (currentState < 0) {
                    mNetworkState = currentState;
                    return;
                }
                if (currentState == mNetworkState) {
                    return;
                }
                mNetworkState = currentState;
                ClientController.handleControll(device.uuid, "disConnect", null);
                device.connectType = Device.CONNECT_TYPE_CHANGE_NETWORK;
                ClientController.handleControll(device.uuid, "reConnect", null);
            }
        }
    }

    private NetworkChangeReceiver mNetworkChangeReceiver;
    private int mNetworkState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (!AppSettings.isFullScreen()) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                StatusBarUtil.transparencyBar(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_full);
        textureViewLayout = findViewById(R.id.texture_view_layout);
        device = ClientController.getDevice(getIntent().getStringExtra("uuid"));
        if (device == null) return;
        ClientController.setFullView(device.uuid, this);
        // 初始化
        setNavBarHide(AppSettings.showVirtualKeys());
        // 按键监听
        setButtonListener();
        // 更新textureView
        textureViewLayout.addView(ClientController.getTextureView(device.uuid), 0);
        mHandler.post(this::updateMaxSize);
        sensorManager = (SensorManager) getApplication().getSystemService(Context.SENSOR_SERVICE);
        // 页面自动旋转
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mInitTime = SystemClock.elapsedRealtime();
        mNetworkState = DeviceTools.getNetworkType();
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_TOUCH_TIME, CHECK_TOUCH_TIME_INTERVAL);
        if (device.leftTime < TimeUnit.DAYS.toMillis(1)) {
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_LEFT_TIME, CHECK_LEFT_TIME_INTERVAL);
        }
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)) {
            registerReceiver(mNetworkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mNetworkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppSettings.sPaused && !AppSettings.sConnected) {
            device.connectType = Device.CONNECT_TYPE_AUTO_CONNECT;
            ClientController.handleControll(device.uuid, "reConnect", null);
        }
        AppSettings.resetLastTouchTime();
        AppSettings.sPaused = false;
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        AppSettings.sPaused = true;
        if (isChangingConfigurations()) {
            textureViewLayout.removeView(ClientController.getTextureView(device.uuid));
        }
//    else if (!isClose) ClientController.handleControll(device.uuid, device.fullToMiniOnRunning ? "changeToMini" : "changeToSmall", ByteBuffer.wrap("changeToFull".getBytes()));
        super.onPause();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        mHandler.post(this::updateMaxSize);
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
    }

    private void updateMaxSize() {
        int width = textureViewLayout.getMeasuredWidth();
        int height = textureViewLayout.getMeasuredHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putInt(width);
        byteBuffer.putInt(height);
        byteBuffer.flip();
        ClientController.handleControll(device.uuid, "updateMaxSize", byteBuffer);
        if (!device.customResolutionOnConnect && device.changeResolutionOnRunning)
            ClientController.handleControll(device.uuid, "writeByteBuffer", ControlPacket.createChangeResolutionEvent((float) width / height));
    }

    public void hide() {
        try {
            isClose = true;
            textureViewLayout.removeView(ClientController.getTextureView(device.uuid));
            finish();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    // 设置按钮监听
    private void setButtonListener() {
        findViewById(R.id.button_back).setOnClickListener(v -> ClientController.handleControll(device.uuid, "buttonBack", null));
        findViewById(R.id.button_home).setOnClickListener(v -> ClientController.handleControll(device.uuid, "buttonHome", null));
        findViewById(R.id.button_switch).setOnClickListener(v -> ClientController.handleControll(device.uuid, "buttonSwitch", null));
        findViewById(R.id.ll_setting).setOnClickListener(v -> {
            showPopupWindow();
        });
        ImageView netIv = findViewById(R.id.iv_net);
        netIv.setImageResource(DeviceTools.isWiFiNet() ? R.drawable.setting_wifi_blue : R.drawable.setting_net_blue);
        TextView msTv = findViewById(R.id.tv_ms);
        mPingUtils.checkPings(device.address, time -> {
            if (AppSettings.sPaused) {
                return;
            }
            mHandler.post(() -> {
                if (!DeviceTools.isNetConnected()) {
                    return;
                }
                if (time > 0) {
                    boolean wifi = DeviceTools.isWiFiNet();
                    msTv.setVisibility(View.VISIBLE);
                    if (time < 50) {
                        netIv.setImageResource(wifi ? R.drawable.setting_wifi_blue : R.drawable.setting_net_blue);
                        msTv.setTextColor(getResources().getColor(R.color.blue));
                    } else if (time < 100) {
                        msTv.setTextColor(getResources().getColor(R.color.orange));
                        netIv.setImageResource(wifi ? R.drawable.setting_wifi_orange : R.drawable.setting_net_orange);
                    } else {
                        msTv.setTextColor(getResources().getColor(R.color.red));
                        netIv.setImageResource(wifi ? R.drawable.setting_wifi_red : R.drawable.setting_net_red);
                    }
                    msTv.setText(String.valueOf(time + "ms"));
                } else {
                    msTv.setVisibility(View.INVISIBLE);
                }
            });
        });
    }

    // 导航栏隐藏
    private void setNavBarHide(boolean isShow) {
        findViewById(R.id.nav_bar).setVisibility(isShow ? View.VISIBLE : View.GONE);
        textureViewLayout.post(this::updateMaxSize);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Sensor.TYPE_ACCELEROMETER != sensorEvent.sensor.getType()
                || textureViewLayout.getMeasuredWidth() < textureViewLayout.getMeasuredHeight())
            return;
        float[] values = sensorEvent.values;
        float x = values[0];
        float y = values[1];
        int newOrientation = getRequestedOrientation();
        if (y > -3 && y < 3 && x >= 4.5) newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        else if (y > -3 && y < 3 && x <= -4.5)
            newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

        if (getRequestedOrientation() != newOrientation) {
            setRequestedOrientation(newOrientation);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int action = event.getAction();

        if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (AppSettings.isBackConfirm()) {
                showBackConfirmDialog();
            } else {
                if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                    ClientController.handleControll(device.uuid, "close", null);
                } else {
                    lastBackPressTime = System.currentTimeMillis();
                    ToastUtils.showToastNoRepeat(R.string.device_exit_tips);
                }
            }

            return true;
        }
        if (action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if (!AppSettings.showVoice()) {
                AppSettings.setShowVoice(true);
                Map<String, Object> params = new HashMap<>();
                params.put("voice", true);
                DeviceTools.fireGlobalEvent("refreshSettings", params);
            }
        }
        if (action == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            ClientController.handleControll(device.uuid, "writeByteBuffer", ControlPacket.createKeyEvent(event.getKeyCode(), event.getMetaState()));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mHandler.removeCallbacksAndMessages(null);
            if (mTimeOutDialog != null && mTimeOutDialog.isShowing()) {
                mTimeOutDialog.dismiss();
            }
            mPingUtils.destroy();
            unregisterReceiver(mNetworkChangeReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showPopupWindow() {
        try {
            View view = View.inflate(this, R.layout.popup_setting, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            boolean isLandscape = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            int width = isLandscape ? DeviceTools.getScreenWidth() * 4 / 5 : DeviceTools.getScreenWidth() * 5 / 6;
            final PopupWindow popupWindow = new PopupWindow(view, width, view
                    .getMeasuredHeight(), true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setTouchable(true);
            TextView phoneTv = view.findViewById(R.id.tv_phone);
            if (!TextUtils.isEmpty(device.name)) {
                phoneTv.setText(device.name);
            }
            RadioGroup radioGroup = view.findViewById(R.id.rg_resolution);
            RadioButton radioButton = null;
            switch (AppSettings.getResolutionType()) {
                case AppSettings.RESOLUTION_HIGH:
                    radioButton = view.findViewById(R.id.rb_high);
                    break;
                case AppSettings.RESOLUTION_SUPER:
                    radioButton = view.findViewById(R.id.rb_super);
                    break;
                default:
                    radioButton = view.findViewById(R.id.rb_common);
                    break;
            }
            radioButton.setChecked(true);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.rb_high) {
                        AppSettings.setResolutionType(AppSettings.RESOLUTION_HIGH);
                    } else if (checkedId == R.id.rb_super) {
                        AppSettings.setResolutionType(AppSettings.RESOLUTION_SUPER);
                    } else {
                        AppSettings.setResolutionType(AppSettings.RESOLUTION_COMMON);
                    }
                    if (AppSettings.sConnected) {
                        ClientController.handleControll(device.uuid, "disConnect", null);
                    }
                    device.connectType = Device.CONNECT_TYPE_CHANGE_RESOLUTION;
                    ClientController.handleControll(device.uuid, "reConnect", null);
                    try {
                        popupWindow.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            TextView voiceTv = view.findViewById(R.id.tv_voice);
            ImageView voiceIv = view.findViewById(R.id.iv_voice);
            voiceTv.setText(AppSettings.showVoice() ? R.string.device_voice_off : R.string.device_voice_on);
            voiceIv.setImageResource(AppSettings.showVoice() ? R.drawable.setting_voice_on : R.drawable.setting_voice_off);
            TextView keyTv = view.findViewById(R.id.tv_key);
            ImageView keyIv = view.findViewById(R.id.iv_key);
            keyTv.setText(AppSettings.showVirtualKeys() ? R.string.device_hide_keys : R.string.device_show_keys);
            keyIv.setImageResource(AppSettings.showVirtualKeys() ? R.drawable.setting_vk_on : R.drawable.setting_vk_off);
            CustomOnClickListener listener = new CustomOnClickListener() {
                @Override
                public void onClickView(View view) {
                    int viewId = view.getId();
                    if (viewId == R.id.ll_voice) {
                        if (AppSettings.showVoice()) {
                            AppSettings.setShowVoice(false);
                            voiceTv.setText(R.string.device_voice_on);
                            voiceIv.setImageResource(R.drawable.setting_voice_off);
                            Map<String, Object> params = new HashMap<>();
                            params.put("voice", false);
                            DeviceTools.fireGlobalEvent("refreshSettings", params);
                        } else {
                            AppSettings.setShowVoice(true);
                            voiceTv.setText(R.string.device_voice_off);
                            voiceIv.setImageResource(R.drawable.setting_voice_on);
                            Map<String, Object> params = new HashMap<>();
                            params.put("voice", true);
                            DeviceTools.fireGlobalEvent("refreshSettings", params);
                        }
                    } else if (viewId == R.id.ll_key) {
                        if (AppSettings.showVirtualKeys()) {
                            AppSettings.setShowVirtualKeys(false);
                            keyTv.setText(R.string.device_show_keys);
                            keyIv.setImageResource(R.drawable.setting_vk_off);
                            setNavBarHide(false);
                        } else {
                            AppSettings.setShowVirtualKeys(true);
                            keyTv.setText(R.string.device_hide_keys);
                            keyIv.setImageResource(R.drawable.setting_vk_on);
                            setNavBarHide(true);

                        }
                    } else if (viewId == R.id.ll_reboot) {
                        popupWindow.dismiss();
                        try {
                            CountDownDialog countDownDialog = new CountDownDialog(FullActivity.this);
                            countDownDialog.setMessageText(R.string.device_reboot_dialog_tips);
                            countDownDialog.setOnClickListener(new CustomDialog.OnClickListener() {
                                @Override
                                public void onConfirmClicked() {
                                    Map<String, Object> params = new HashMap<>();
                                    params.put("uuid", device.uuid);
                                    params.put("sourceId",device.sourceId);
                                    DeviceTools.fireGlobalEvent("reboot", params);
                                    ClientController.handleControll(device.uuid, "close", null);
                                }
                            });
                            countDownDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (viewId == R.id.ll_exit) {
                        popupWindow.dismiss();
                        ClientController.handleControll(device.uuid, "close", null);
                    } else if (viewId == R.id.iv_home) {
                        ClientController.handleControll(device.uuid, "buttonHome", null);
                    } else if (viewId == R.id.iv_switch) {
                        ClientController.handleControll(device.uuid, "buttonSwitch", null);
                    } else if (viewId == R.id.iv_back) {
                        ClientController.handleControll(device.uuid, "buttonBack", null);
                    }
                }
            };

            view.findViewById(R.id.ll_voice).setOnClickListener(listener);
            view.findViewById(R.id.ll_key).setOnClickListener(listener);
            view.findViewById(R.id.ll_reboot).setOnClickListener(listener);
            view.findViewById(R.id.ll_exit).setOnClickListener(listener);
            view.findViewById(R.id.iv_home).setOnClickListener(listener);
            view.findViewById(R.id.iv_switch).setOnClickListener(listener);
            view.findViewById(R.id.iv_back).setOnClickListener(listener);

            popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showBackConfirmDialog() {
        try {
            CustomDialog customDialog = new CustomDialog(this);
            customDialog.setMessageText(R.string.device_exit_dialog_tips).setCheckBoxVisible().setOnClickListener(new CustomDialog.OnClickListener() {
                @Override
                public void onConfirmClicked() {
                    customDialog.dismiss();
                    if (customDialog.isChecked()) {
                        AppSettings.setBackConfirm(false);
                        Map<String, Object> params = new HashMap<>();
                        params.put("backConfirm", false);
                        DeviceTools.fireGlobalEvent("refreshSettings", params);
                    }
                    ClientController.handleControll(device.uuid, "close", null);
                }
            });
            customDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CustomDialog mLeftTimeDialog;

    private boolean showLeftTimeDialog() {
        try {
            if (!AppSettings.sConnected || !DeviceTools.isNetConnected()) {
                return false;
            }
            if (device.leftTime <= 0 || mLeftTimeDialog != null && mLeftTimeDialog.isShowing()) {
                return false;
            }
            long leftTime = device.leftTime - (SystemClock.elapsedRealtime() - mInitTime);
            if (leftTime <= 0) {
                if (mLeftTimeDialog == null) {
                    mLeftTimeDialog = new CustomDialog(this);
                }
                ClientController.handleControll(device.uuid, "disConnect", null);
                mLeftTimeDialog.setMessageText(R.string.device_expire_tips).setConfirmText(R.string.device_exit)
                        .setCancelVisibility(View.GONE).setOnClickListener(new CustomDialog.OnClickListener() {
                            @Override
                            public void onConfirmClicked() {
                                ClientController.handleControll(device.uuid, "close", null);
                            }
                        });
                mLeftTimeDialog.show();
                mHandler.removeCallbacksAndMessages(MSG_CHECK_LEFT_TIME);
                return true;
            } else if (leftTime <= TimeUnit.MINUTES.toMillis(60) && leftTime >= TimeUnit.MINUTES.
                    toMillis(5) && !DeviceTools.hasShowRechargeTips(device.uuid)) {//5-60分钟之间提示用户去续费
                if (mLeftTimeDialog == null) {
                    mLeftTimeDialog = new CustomDialog(this);
                }
                String tips = String.format(getString(R.string.device_left_time_tips), TimeUnit.MINUTES.
                        convert(leftTime, TimeUnit.MILLISECONDS));
                mLeftTimeDialog.setMessageText(tips).setCancelVisibility(View.VISIBLE).setConfirmText(R.string.device_recharge)
                        .setOnClickListener(new CustomDialog.OnClickListener() {
                            @Override
                            public void onCancelClicked() {
                                mHandler.sendEmptyMessageDelayed(MSG_CHECK_LEFT_TIME, CHECK_LEFT_TIME_INTERVAL);
                            }

                            @Override
                            public void onConfirmClicked() {
                                mLeftTimeDialog.dismiss();
                                Map<String, Object> params = new HashMap<>();
                                params.put("uuid", device.uuid);
                                DeviceTools.fireGlobalEvent("recharge", params);
                                ClientController.handleControll(device.uuid, "close", null);
                            }
                        });
                mLeftTimeDialog.show();
                DeviceTools.saveShowRechargeTipsTime(device.uuid);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private CustomDialog mTimeOutDialog;

    private boolean showNoHandleTimeOut() {
        if (mLeftTimeDialog != null && mLeftTimeDialog.isShowing()) {
            return false;
        }
        if (mTimeOutDialog != null && mTimeOutDialog.isShowing()) {
            return false;
        }
        if (!AppSettings.showTimeOutDialog()) {
            return false;
        }
        try {
            if (AppSettings.sConnected) {
                ClientController.handleControll(device.uuid, "disConnect", null);
            }
            if (!AppSettings.sPaused) {
                if (mTimeOutDialog == null) {
                    mTimeOutDialog = new CustomDialog(this).setMessageText(R.string.disconnect_tips)
                            .setTitleText(R.string.title_tips).setCancelText(R.string.device_exit)
                            .setConfirmText(R.string.reconnect_device).setOnClickListener(new CustomDialog.OnClickListener() {
                                @Override
                                public void onCancelClicked() {
                                    ClientController.handleControll(device.uuid, "close", null);
                                }

                                @Override
                                public void onConfirmClicked() {
                                    if (!DeviceTools.isNetConnected()) {
                                        ToastUtils.showToastNoRepeat(R.string.connect_net_error);
                                        return;
                                    }
                                    mTimeOutDialog.dismiss();
                                    mHandler.sendEmptyMessageDelayed(MSG_CHECK_TOUCH_TIME, CHECK_TOUCH_TIME_INTERVAL);
                                    device.connectType = Device.CONNECT_TYPE_RECONNECT;
                                    ClientController.handleControll(device.uuid, "reConnect", null);
                                }
                            });
                }
                mTimeOutDialog.show();
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}