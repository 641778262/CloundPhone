package com.feihu.cp.client.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;


/**
 * Created by json on 17/3/1.
 */

public class PressTextView extends TextView {
    public PressTextView(Context context) {
        super(context);
    }

    public PressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PressTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Drawable drawable = getBackground();
        if(drawable != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    drawable.setColorFilter(new PorterDuffColorFilter(Color.argb(50, 0, 0, 0), PorterDuff.Mode.SRC_ATOP));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    drawable.setColorFilter(new PorterDuffColorFilter(Color.argb(0, 0, 0, 0), PorterDuff.Mode.SRC_ATOP));
                    break;
                default:
                    break;
            }
        }

        return super.onTouchEvent(event);
    }
}
