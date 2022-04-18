package com.c.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DispatchView extends androidx.appcompat.widget.AppCompatTextView {

    private static final String TAG = "DispatchView";

    public DispatchView(@NonNull Context context) {
        super(context);
    }

    public DispatchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DispatchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, "dispatchKeyEvent: " + event.getAction() + ", code: " + event.getKeyCode());
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyUp: " + event.getAction() + ", code: " + event.getKeyCode());
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown: " + event.getAction() + ", code: " + event.getKeyCode());
        return super.onKeyDown(keyCode, event);
    }
}
