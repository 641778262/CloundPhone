package com.feihu.cp.client.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.feihu.cp.R;


public class CustomDialog extends AlertDialog {

    private OnClickListener listener;

    private TextView tvCancel;
    private TextView tvConfirm;

    private TextView tvTitle;
    private TextView tvContent;
    private CheckBox cb;

    private String title;
    private String message;

    private String cancelText;
    private String confirmText;
    private boolean cbVisible;

    public CustomDialog(Context context) {
        super(context, R.style.CustomDialog);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        //设置对话框的显示位置
        setDialogStartPosition();
        //绑定控件
        initView();
        //初始化个组件的内容
        initText();
        //设置点击事件触发
        initEvent();

    }

    private void setDialogStartPosition() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置起始位置
//        lp.x  = 100;
//        lp.y = 100;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);

    }

    private void initEvent() {
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onCancelClick();
                }
            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirmClick();
                }
            }
        });
    }

    private void initText() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        if (!TextUtils.isEmpty(message)) {
            tvContent.setText(message);
        }
        if (!TextUtils.isEmpty(cancelText)) {
            tvCancel.setText(cancelText);
        }
        if (!TextUtils.isEmpty(confirmText)) {
            tvConfirm.setText(confirmText);
        }
        if(cbVisible) {
            cb.setVisibility(View.VISIBLE);
        }
    }

    public CustomDialog setTitleText(String title) {
        this.title = title;
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        return this;
    }

    public CustomDialog setTitleText(int titleId) {
        this.title = getContext().getString(titleId);
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        return this;
    }

    public CustomDialog setMessageText(String message) {
        this.message = message;
        if (tvContent != null) {
            tvContent.setText(message);
        }
        return this;
    }

    public CustomDialog setMessageText(int message) {
        this.message = getContext().getString(message);
        if (tvContent != null) {
            tvContent.setText(message);
        }
        return this;
    }

    public CustomDialog setCancelText(String cancelText) {
        this.cancelText = cancelText;
        if (tvCancel != null) {
            tvCancel.setText(cancelText);
        }
        return this;
    }

    public CustomDialog setCancelText(int textId) {
        this.cancelText = getContext().getString(textId);
        if (tvCancel != null) {
            tvCancel.setText(cancelText);
        }
        return this;
    }

    public CustomDialog setConfirmText(String confirmText) {
        this.confirmText = confirmText;
        if (tvConfirm != null) {
            tvConfirm.setText(confirmText);
        }
        return this;
    }

    public CustomDialog setConfirmText(int textId) {
        this.confirmText = getContext().getString(textId);
        if (tvConfirm != null) {
            tvConfirm.setText(confirmText);
        }
        return this;
    }

    public CustomDialog setCheckBoxVisible() {
        this.cbVisible = true;
        return this;
    }

    public boolean isChecked(){
        return cb!= null && cb.isChecked();
    }

    private void initView() {
        cb = findViewById(R.id.cb_tips);
        tvCancel = findViewById(R.id.tv_cancel);
        tvConfirm = findViewById(R.id.tv_confirm);
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
    }


    //提供给外部使用的方法
    public CustomDialog setOnClickListener(OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnClickListener {
        void onCancelClick();

        void onConfirmClick();
    }
}


