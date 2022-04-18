package com.c.sample.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class CameraManagerImpl implements CameraManagerFacade {

    private CameraManager mCameraManager;

    public CameraManagerImpl(CameraManager cameraManager) {
        this.mCameraManager = cameraManager;
    }

    @Override
    public Facing getFacing(String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
        switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                return Facing.FACING_FRONT;
            case CameraCharacteristics.LENS_FACING_BACK:
                return Facing.FACING_BACK;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                return Facing.FACING_EXTERNAL;
            default:
                return Facing.FACING_UNKNOWN;
        }
    }
}
