package com.c.core;

import android.hardware.camera2.CameraAccessException;

public interface Camera {

    void open(String id) throws CameraAccessException, NoPermissionException;

    void close();

    void takePicture();
}
