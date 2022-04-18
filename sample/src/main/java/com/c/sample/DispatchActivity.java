package com.c.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DispatchActivity extends AppCompatActivity {

    private static final String TAG = "DispatchActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i(TAG, "dispatchKeyEvent: " + event.getAction() + ", code: " + event.getKeyCode());
        if (event.getAction() == 0 && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown: " + event.getAction() + ", code: " + event.getKeyCode());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyUp: " + event.getAction() + ", code: " + event.getKeyCode());
        return super.onKeyUp(keyCode, event);
    }
}
