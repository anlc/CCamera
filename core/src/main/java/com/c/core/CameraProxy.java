package com.c.core;

import android.content.Context;
import android.hardware.camera2.CameraManager;

public class CameraProxy {

    private Context mContext;

    public CameraProxy(Context context) {
        this.mContext = context;

        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }
}
