package top.saymzx.easycontrol.app.helper;

import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import top.saymzx.easycontrol.app.R;
import top.saymzx.easycontrol.app.entity.AppData;

public class ToastUtils {
    private static Toast mToast;
    public static void showToastNoRepeat(String text) {
        try {
            if (mToast == null) {
                initToast();
            }
            TextView textView = (TextView) mToast.getView();
            textView.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showToastNoRepeat(int resId) {
        showToastNoRepeat(AppData.applicationContext.getResources().getString(resId));
    }

    public static void initToast() {
        mToast = new Toast(AppData.applicationContext);
        TextView textView = new TextView(AppData.applicationContext);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setMinimumWidth(DeviceTools.getScreenWidth() / 4);
        textView.setTextColor(AppData.applicationContext.getResources().getColor(R.color.white));
        textView.setBackgroundResource(R.drawable.toast_bg);
        mToast.setView(textView);
        int padding = DeviceTools.dp2px(20);
        textView.setPadding(padding, padding / 2, padding, padding / 2);
        mToast.setGravity(Gravity.CENTER, 0, 0);
    }
}
