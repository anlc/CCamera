package com.c.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class CameraApi2 implements Camera {

    private Context mContext;
    private final CameraManager mCameraManager;
    private Handler mHandler;

    public CameraApi2(Context context) {
        this.mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        HandlerThread camera = new HandlerThread("camera");
        camera.start();
        mHandler = new Handler(camera.getLooper());
    }

    @Override
    public void open(String id) throws CameraAccessException, NoPermissionException {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw new NoPermissionException();
        }
        CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {

            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        };
        mCameraManager.openCamera(id, callback, mHandler);
    }

    @Override
    public void close() {

    }

    @Override
    public void takePicture() {

    }
}
