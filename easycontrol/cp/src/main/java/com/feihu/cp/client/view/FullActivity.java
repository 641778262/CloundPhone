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
import android.media.AudioManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
//        try {
//            if (!DeviceTools.isLandscape() && !AppSettings.isFullScreen()) {
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                StatusBarUtil.transparencyBar(this);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        AppSettings.sPaused = false;
        setContentView(R.layout.activity_full);
        textureViewLayout = findViewById(R.id.texture_view_layout);
        device = ClientController.getDevice(getIntent().getStringExtra("uuid"));
        if (device == null) return;
        ClientController.setFullView(device.uuid, this);
        // 初始化
        setNavBarHide(AppSettings.showVirtualKeys());
        // 按键监听
        setButtonListener();
        initControlMode(0);
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
            device.connectType = Device.CONNECT_TYPE_RECONNECT;
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
        CustomOnClickListener listener = new CustomOnClickListener() {
            @Override
            public void onClickView(View view) {
                int id = view.getId();
                if (id == R.id.button_back) {
                    ClientController.handleControll(device.uuid, "buttonBack", null);
                } else if (id == R.id.button_home) {
                    ClientController.handleControll(device.uuid, "buttonHome", null);
                } else if (id == R.id.button_switch) {
                    ClientController.handleControll(device.uuid, "buttonSwitch", null);
                } else if (id == R.id.ll_setting) {
                    showSettingPopupWindow();
                }
            }
        };
        findViewById(R.id.button_back).setOnClickListener(listener);
        findViewById(R.id.button_home).setOnClickListener(listener);
        findViewById(R.id.button_switch).setOnClickListener(listener);
        findViewById(R.id.ll_setting).setOnClickListener(listener);
        ImageView netIv = findViewById(R.id.iv_net);
        ImageView topNetIv = findViewById(R.id.iv_net_top);
        netIv.setImageResource(DeviceTools.isWiFiNet() ? R.drawable.setting_wifi_blue : R.drawable.setting_net_blue);
        topNetIv.setImageResource(DeviceTools.isWiFiNet() ? R.drawable.setting_wifi_blue : R.drawable.setting_net_blue);
        TextView msTv = findViewById(R.id.tv_ms);
        TextView topMsTv = findViewById(R.id.tv_ms_top);
        mPingUtils.checkPings(device.address, time -> {
            if (AppSettings.sPaused) {
                return;
            }
            mHandler.post(() -> {
                initNetPings(netIv, msTv, time);
                initNetPings(topNetIv, topMsTv, time);
            });
        });
    }

    private void initNetPings(ImageView netIv, TextView msTv, int time) {
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
                    exit();
                } else {
                    lastBackPressTime = System.currentTimeMillis();
                    ToastUtils.showToastNoRepeat(R.string.device_exit_tips);
                }
            }

            return true;
        }
        if (action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            restoreSound();
        }
        if (action == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            ClientController.handleControll(device.uuid, "writeByteBuffer", ControlPacket.createKeyEvent(event.getKeyCode(), event.getMetaState()));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void restoreSound() {
        if (!AppSettings.showVoice()) {
            AppSettings.setShowVoice(true);
            Map<String, Object> params = new HashMap<>();
            params.put("voice", true);
            DeviceTools.fireGlobalEvent("refreshSettings", params);
        }
        if (mSettingPopupWindow != null && mSettingPopupWindow.isShowing()) {
            ImageView voiceIv = mSettingPopupWindow.getContentView().findViewById(R.id.iv_voice);
            voiceIv.setImageResource(AppSettings.showVoice() ? R.drawable.setting_voice_on : R.drawable.setting_voice_off);

        }
        if (!AppSettings.isDefaultControlMode()) {
            mRightVoiceIv.setImageResource(AppSettings.showVoice() ? R.drawable.setting_voice_on : R.drawable.setting_voice_off);
        }
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

    private void showResolutionPopupWindow() {
        View view = View.inflate(this, R.layout.popup_resolution, null);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        CustomPopupWindow popupWindow = new CustomPopupWindow(view, view.getMeasuredWidth(), view
                .getMeasuredHeight(), true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        changeResolution(view, popupWindow);
    }

    private void changeResolution(View view, PopupWindow popupWindow) {
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
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
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
            if (popupWindow != mSettingPopupWindow) {
                initTopResolutionTv();
            }
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private CustomPopupWindow mSettingPopupWindow;

    private void showSettingPopupWindow() {
        try {
            View view = View.inflate(this, R.layout.popup_setting, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            boolean isLandscape = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            int width = isLandscape ? DeviceTools.getScreenWidth() * 4 / 5 : DeviceTools.getScreenWidth() * 5 / 6;
            mSettingPopupWindow = new CustomPopupWindow(view, width, view
                    .getMeasuredHeight(), true);
            mSettingPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mSettingPopupWindow.setOutsideTouchable(true);
            mSettingPopupWindow.setTouchable(true);
            TextView phoneTv = view.findViewById(R.id.tv_phone);
            if (!TextUtils.isEmpty(device.name)) {
                phoneTv.setText(device.name);
            }
            ImageView vipIv = view.findViewById(R.id.iv_vip);
            vipIv.setImageResource(device.getVipResourceId());
            changeResolution(view, mSettingPopupWindow);
            ImageView voiceIv = view.findViewById(R.id.iv_voice);
            voiceIv.setImageResource(AppSettings.showVoice() ? R.drawable.setting_voice_on : R.drawable.setting_voice_off);
            ImageView keyIv = view.findViewById(R.id.iv_key);
            keyIv.setImageResource(AppSettings.showVirtualKeys() ? R.drawable.setting_vk_on : R.drawable.setting_vk_off);
            CustomOnClickListener listener = new CustomOnClickListener() {
                @Override
                public void onClickView(View view) {
                    int viewId = view.getId();
                    if (viewId == R.id.ll_voice) {
                        handleVoice(voiceIv, R.drawable.setting_voice_off, R.drawable.setting_voice_on);
                    } else if (viewId == R.id.ll_key) {
                        handleVk(keyIv, R.drawable.setting_vk_off, R.drawable.setting_vk_on);
                    } else if (viewId == R.id.ll_reboot) {
                        mSettingPopupWindow.dismiss();
                        rebootDevice();
                    } else if (viewId == R.id.ll_exit) {
                        mSettingPopupWindow.dismiss();
                        exit();
                    } else if (viewId == R.id.iv_home) {
                        ClientController.handleControll(device.uuid, "buttonHome", null);
                    } else if (viewId == R.id.iv_switch) {
                        ClientController.handleControll(device.uuid, "buttonSwitch", null);
                    } else if (viewId == R.id.iv_back) {
                        ClientController.handleControll(device.uuid, "buttonBack", null);
                    } else if (viewId == R.id.tv_professional_mode) {
                        mSettingPopupWindow.dismiss();
                        initControlMode(AppSettings.CONTROL_MODE_PROFESSIONAL);
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
            view.findViewById(R.id.tv_professional_mode).setOnClickListener(listener);
            view.setFocusableInTouchMode(true);
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || keyEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                        restoreSound();
                    }
                    return false;
                }
            });
            mSettingPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleVk(ImageView keyIv, int vkOffResId, int vkOnResId) {
        if (AppSettings.showVirtualKeys()) {
            AppSettings.setShowVirtualKeys(false);
            keyIv.setImageResource(vkOffResId);
            setNavBarHide(false);
        } else {
            AppSettings.setShowVirtualKeys(true);
            keyIv.setImageResource(vkOnResId);
            setNavBarHide(true);

        }
    }

    private void handleVoice(ImageView voiceIv, int voiceOffResId, int voiceOnResId) {
        if (AppSettings.showVoice()) {
            AppSettings.setShowVoice(false);
            voiceIv.setImageResource(voiceOffResId);
            Map<String, Object> params = new HashMap<>();
            params.put("voice", false);
            DeviceTools.fireGlobalEvent("refreshSettings", params);
        } else {
            AppSettings.setShowVoice(true);
            voiceIv.setImageResource(voiceOnResId);
            Map<String, Object> params = new HashMap<>();
            params.put("voice", true);
            DeviceTools.fireGlobalEvent("refreshSettings", params);
        }
    }

    private void rebootDevice() {
        try {
            CountDownDialog countDownDialog = new CountDownDialog(FullActivity.this);
            countDownDialog.setMessageText(R.string.device_reboot_dialog_tips);
            countDownDialog.setOnClickListener(new CustomDialog.OnClickListener() {
                @Override
                public void onConfirmClicked() {
                    Map<String, Object> params = new HashMap<>();
                    params.put("uuid", device.uuid);
                    params.put("machineCode", device.sourceId);
                    DeviceTools.fireGlobalEvent("reboot", params);
                    exit();
                }
            });
            countDownDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exit() {
        ClientController.handleControll(device.uuid, "close", null);
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
                    exit();
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
                                exit();
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
                                exit();
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
                                    exit();
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

    private LinearLayout mRightLayout;
    private LinearLayout mTopLayout;
    private LinearLayout mSettingLayout;
    private ImageView mRightVoiceIv;
    private ImageView mRightVkIv;

    private void initControlMode(int mode) {
        if (mSettingLayout == null || mTopLayout == null || mRightLayout == null) {
            mSettingLayout = findViewById(R.id.ll_setting);
            mTopLayout = findViewById(R.id.ll_top);
            mRightLayout = findViewById(R.id.ll_right);
            mRightVoiceIv = findViewById(R.id.iv_voice_right);
            mRightVkIv = findViewById(R.id.iv_vk_right);
            TextView nameTv = findViewById(R.id.tv_name_top);
            nameTv.setText(device.name);
            ImageView vipIv = findViewById(R.id.iv_vip_top);
            vipIv.setImageResource(device.getVipResourceId());
            CustomOnClickListener listener = new CustomOnClickListener() {
                @Override
                public void onClickView(View view) {
                    int id = view.getId();
                    if (id == R.id.tv_resolution_top) {
                        showResolutionPopupWindow();
                    } else if (id == R.id.ll_switch_mode_right) {
                        initControlMode(AppSettings.CONTROL_MODE_DEFAULT);
                    } else if (id == R.id.ll_voice_right) {
                        handleVoice(mRightVoiceIv, R.drawable.setting_voice_off, R.drawable.setting_voice_on);
                    } else if (id == R.id.ll_vk_right) {
                        handleVk(mRightVkIv, R.drawable.setting_vk_off, R.drawable.setting_vk_on);
                    } else if (id == R.id.ll_reboot_right) {
                        rebootDevice();
                    } else if (id == R.id.ll_exit_right) {
                        exit();
                    }
                }
            };
            findViewById(R.id.tv_resolution_top).setOnClickListener(listener);
            findViewById(R.id.ll_switch_mode_right).setOnClickListener(listener);
            findViewById(R.id.ll_voice_right).setOnClickListener(listener);
            findViewById(R.id.ll_vk_right).setOnClickListener(listener);
            findViewById(R.id.ll_reboot_right).setOnClickListener(listener);
            findViewById(R.id.ll_exit_right).setOnClickListener(listener);
            findViewById(R.id.ll_voice_plus_right).setOnClickListener(view -> {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                restoreSound();
            });
            findViewById(R.id.ll_voice_minus_right).setOnClickListener(view -> {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                restoreSound();
            });
        }
        if (mode > 0) {
            AppSettings.setControlMode(mode);
            Map<String, Object> params = new HashMap<>();
            params.put("fullScreen", mode == AppSettings.CONTROL_MODE_DEFAULT);
            DeviceTools.fireGlobalEvent("refreshSettings", params);
        }
        if (AppSettings.isDefaultControlMode()) {
            mSettingLayout.setVisibility(View.VISIBLE);
            mTopLayout.setVisibility(View.GONE);
            mRightLayout.setVisibility(View.GONE);
        } else {
            mSettingLayout.setVisibility(View.GONE);
            mTopLayout.setVisibility(View.VISIBLE);
            mRightLayout.setVisibility(View.VISIBLE);
            initTopResolutionTv();
            mRightVoiceIv.setImageResource(AppSettings.showVoice() ? R.drawable.setting_voice_on : R.drawable.setting_voice_off);
            mRightVkIv.setImageResource(AppSettings.showVirtualKeys() ? R.drawable.setting_vk_on : R.drawable.setting_vk_off);
            setNavBarHide(true);
        }

        if (mode > 0) {
            textureViewLayout.post(this::updateMaxSize);
        }
    }

    private void initTopResolutionTv() {
        TextView resolutionTv = findViewById(R.id.tv_resolution_top);
        switch (AppSettings.getResolutionType()) {
            case AppSettings.RESOLUTION_HIGH:
                resolutionTv.setText(R.string.device_resolution_high_top);
                break;
            case AppSettings.RESOLUTION_SUPER:
                resolutionTv.setText(R.string.device_resolution_super_top);
                break;
            default:
                resolutionTv.setText(R.string.device_resolution_common_top);
                break;
        }
    }
}