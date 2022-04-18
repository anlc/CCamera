package com.c.sample.camera;

import android.hardware.camera2.CameraAccessException;

public interface CameraManagerFacade {

    enum Facing {
        FACING_BACK,
        FACING_FRONT,
        FACING_EXTERNAL,
        FACING_UNKNOWN // returned if the Camera API returned an error or an unknown type
    }

    Facing getFacing(String cameraId) throws CameraAccessException;
}
