package top.saymzx.easycontrol.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import com.feihu.cp.entity.AppData;
import com.feihu.cp.helper.PublicTools;

import java.util.ArrayList;

import top.saymzx.easycontrol.app.databinding.ActivityIpBinding;
import top.saymzx.easycontrol.app.databinding.ItemTextBinding;


public class IpActivity extends Activity {
  private ActivityIpBinding activityIpBinding;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ViewTools.setStatusAndNavBar(this);
    ViewTools.setLocale(this);
    activityIpBinding = ActivityIpBinding.inflate(this.getLayoutInflater());
    setContentView(activityIpBinding.getRoot());
    setButtonListener();
    // 绘制UI
    drawUi();
    super.onCreate(savedInstanceState);
  }

  private void drawUi() {
    // 添加IP
    Pair<ArrayList<String>, ArrayList<String>> listPair = PublicTools.getIp();
    for (String i : listPair.first) {
      ItemTextBinding text = ViewTools.createTextCard(this, i, () -> {
        PublicTools.getClipboardManager().setPrimaryClip(ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, i));
        Toast.makeText(this, getString(R.string.toast_copy), Toast.LENGTH_SHORT).show();
      });
      activityIpBinding.ipv4.addView(text.getRoot());
    }
    for (String i : listPair.second) {
      ItemTextBinding text = ViewTools.createTextCard(this, i, () -> {
        PublicTools.getClipboardManager().setPrimaryClip(ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, i));
        Toast.makeText(this, getString(R.string.toast_copy), Toast.LENGTH_SHORT).show();
      });
      activityIpBinding.ipv6.addView(text.getRoot());
    }
  }

  // 设置返回按钮监听
  private void setButtonListener() {
    activityIpBinding.backButton.setOnClickListener(v -> finish());
  }

}
