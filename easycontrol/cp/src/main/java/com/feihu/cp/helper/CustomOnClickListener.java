package com.feihu.cp.helper;

import android.view.View;

/**
 * 防止重复点击
 */
public abstract class CustomOnClickListener implements View.OnClickListener{
    @Override
    public void onClick(View v) {
        if(v != null && ButtonUtils.isFastDoubleClick(v.getId())) {
            return;
        }
        onClickView(v);
    }
    public abstract void onClickView(View view);
}
