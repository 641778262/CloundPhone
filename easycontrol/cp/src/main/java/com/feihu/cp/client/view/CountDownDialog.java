package com.feihu.cp.client.view;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.feihu.cp.R;

public class CountDownDialog extends CustomDialog {

    private CountDownTimer mTimer;

    public CountDownDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvConfirm.setBackgroundResource(R.drawable.dialog_confirm_btn_unclickable_bg);
        tvConfirm.setEnabled(false);
        tvConfirm.setHandleTouch(false);
        mTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long time) {
                tvConfirm.setText(String.format(getContext().getString(R.string.confirm_count), time / 1000 + 1));
            }

            @Override
            public void onFinish() {
                tvConfirm.setEnabled(true);
                tvConfirm.setHandleTouch(true);
                tvConfirm.setBackgroundResource(R.drawable.dialog_confirm_btn_bg);
                tvConfirm.setText(R.string.confirm);
            }
        };
        mTimer.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }
}
