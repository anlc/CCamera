package com.c.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class ParentLayout extends LinearLayout {

    private static final String TAG = "ParentLayout";
    private int mLastX;
    private int mLastY;
    private int mLastXIntercept;
    private int mLastYIntercept;
    private final static int SLIDE_XY_THRESHOLD = 3;

    public ParentLayout(Context context) {
        super(context);
    }

    public ParentLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ParentLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                intercepted = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                int delta = Math.abs(deltaX) - Math.abs(deltaY);
                Log.i(TAG, "onInterceptTouchEvent: " + (deltaX > 0 && delta > SLIDE_XY_THRESHOLD));
                if (deltaX > 0 && delta > SLIDE_XY_THRESHOLD) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
            }
            default:
                break;
        }
        mLastXIntercept = x;
        mLastYIntercept = y;
        mLastX = x;
        mLastY = y;
        return intercepted;
    }
}
