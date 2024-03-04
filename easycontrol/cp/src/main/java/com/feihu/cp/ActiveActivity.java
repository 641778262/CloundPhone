package com.feihu.cp;

import android.app.Activity;
import android.os.Bundle;

import com.feihu.cp.databinding.ActivityActiveBinding;
import com.feihu.cp.entity.AppData;
import com.feihu.cp.helper.ViewTools;

public class ActiveActivity extends Activity {

  private ActivityActiveBinding activityActiveBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ViewTools.setStatusAndNavBar(this);
    ViewTools.setLocale(this);
    activityActiveBinding = ActivityActiveBinding.inflate(this.getLayoutInflater());
    setContentView(activityActiveBinding.getRoot());
    // 取消激活
//    if (AppData.setting.getIsActive()) deactivate();
//    setButtonListener();
    // 绘制UI
    drawUi();
    super.onCreate(savedInstanceState);
  }

  private void drawUi() {
    activityActiveBinding.key.setText(AppData.setting.getActiveKey());
//    activityActiveBinding.url.setOnClickListener(v -> PublicTools.startUrl(this, "https://gitee.com/mingzhixianweb/easycontrol/blob/master/DONATE.md"));
  }

//  private void setButtonListener() {
//    activityActiveBinding.active.setOnClickListener(v -> {
//      String activeKey = String.valueOf(activityActiveBinding.key.getText());
//      AppData.setting.setActiveKey(activeKey);
//      Pair<View, WindowManager.LayoutParams> loading = ViewTools.createLoading(this);
//      AppData.windowManager.addView(loading.first, loading.second);
//      new Thread(() -> {
//        boolean isOk = ActiveHelper.active(activeKey);
//        AppData.windowManager.removeView(loading.first);
//        AppData.uiHandler.post(() -> {
//          if (isOk) {
//            finish();
//            AppData.setting.setIsActive(true);
//            PublicTools.startUrl(this, "https://gitee.com/mingzhixianweb/easycontrol/blob/master/HOW_TO_USE.md");
//            PublicTools.logToast("active", getString(R.string.toast_success), true);
//          } else PublicTools.logToast("active", getString(R.string.toast_fail), true);
//        });
//      }).start();
//    });
//  }

  // 取消激活
//  private void deactivate() {
//    Pair<View, WindowManager.LayoutParams> loading = ViewTools.createLoading(this);
//    AppData.windowManager.addView(loading.first, loading.second);
//    new Thread(() -> {
//      boolean isOk = ActiveHelper.deactivate(AppData.setting.getActiveKey());
//      AppData.windowManager.removeView(loading.first);
//      AppData.uiHandler.post(() -> {
//        if (isOk) {
//          AppData.setting.setIsActive(false);
//          PublicTools.logToast("deactivate", getString(R.string.toast_success), true);
//        } else PublicTools.logToast("deactivate", getString(R.string.toast_fail), true);
//      });
//    }).start();
//  }

//  @Override
//  public void onBackPressed() {
//  }
}