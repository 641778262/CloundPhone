package com.feihu.cp.helper;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.feihu.cp.R;
import com.feihu.cp.entity.AppData;


public class ToastUtils {
//    private static Toast mToast;

    public static void showToastNoRepeat(String text) {
//        try {
//            if (mToast == null) {
//                mToast = initToast(AppData.applicationContext);
//            }
//            showToast(mToast, text);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        showToast(initToast(AppData.applicationContext), text);
    }

    private static void showToast(Toast toast, String text) {
        try {
            TextView textView = (TextView) toast.getView();
            textView.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToastNoRepeat(int resId) {
        showToastNoRepeat(AppData.applicationContext.getResources().getString(resId));
    }

//    /**
//     * 每次新建一个Toast 防止有些界面内存泄露
//     *
//     * @param resId
//     */
//    public static void showCommonText(int resId) {
//        showToast(initToast(AppData.applicationContext), AppData.applicationContext.getResources().getString(resId));
//    }

    public static Toast initToast(Context context) {
        Toast toast = new Toast(context.getApplicationContext());
        TextView textView = new TextView(context.getApplicationContext());
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setMinimumWidth(DeviceTools.getScreenWidth() / 4);
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setBackgroundResource(R.drawable.toast_bg);
        toast.setView(textView);
        int padding = DeviceTools.dp2px(20);
        textView.setPadding(padding, padding / 2, padding, padding / 2);
        toast.setGravity(Gravity.CENTER, 0, 0);
        return toast;
    }
}
