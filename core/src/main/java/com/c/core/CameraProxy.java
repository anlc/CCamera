package com.c.core;

import android.hardware.camera2.CameraAccessException;

public class CameraProxy {

    private Camera mCamera;

    public CameraProxy(Camera camera) {
        mCamera = camera;
    }

    public void open(String id) throws CameraAccessException {
        mCamera.open(id);
    }
}
