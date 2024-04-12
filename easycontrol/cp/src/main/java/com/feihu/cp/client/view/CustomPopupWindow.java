package com.feihu.cp.client.view;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class CustomPopupWindow extends PopupWindow {
    public CustomPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
    }

    private static final float ALPHA = 0.6f;

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        backgroundAlpha(ALPHA);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        backgroundAlpha(ALPHA);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        backgroundAlpha(ALPHA);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        backgroundAlpha(ALPHA);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        backgroundAlpha(1);
    }


    private void backgroundAlpha(float alpha) {
        if (alpha < 0 || alpha > 1) {
            return;
        }
        if (getContentView() == null || !(getContentView().getContext() instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) getContentView().getContext();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = alpha;
        activity.getWindow().setAttributes(lp);
    }
}
