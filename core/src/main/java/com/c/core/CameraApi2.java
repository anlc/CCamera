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

    private HandlerThread mCameraThread;
    private Handler mHandler;
    private CameraManager mCameraManager;
    private final Context mContext;

    public CameraApi2(Context context) {
        this.mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraThread = new HandlerThread("camera");
        mCameraThread.start();
        mHandler = new Handler(mCameraThread.getLooper());
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
        if (mCameraThread != null) {
            try {
                mCameraThread.quitSafely();
                mCameraThread.join();
                mCameraThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void takePicture() {

    }
}
