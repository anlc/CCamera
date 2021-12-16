package com.c.sample;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;

public class TextureViewActivity extends AppCompatActivity {

    private static final String TAG = "TextureViewActivity";
    private CameraManager mCameraManager;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        HandlerThread camera = new HandlerThread("camera");
        camera.start();
        mHandler = new Handler(camera.getLooper());

        TextureView mTvPreview = findViewById(R.id.tv_preview);
        mTvPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                try {
                    openCamera(new Surface(surface));
                } catch (CameraAccessException e) {
                    toast("camera open failed");
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void openCamera(Surface surface) throws CameraAccessException {
        CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.i(TAG, "onOpened: ");
                try {
                    initPreviewRequest(camera, surface);
                } catch (CameraAccessException e) {
                    toast("init preview failed");
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.i(TAG, "onDisconnected: ");
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.i(TAG, "onError: " + error);
            }
        };
        mCameraManager.openCamera(String.valueOf(CameraCharacteristics.LENS_FACING_BACK), stateCallback, mHandler);
    }

    private void initPreviewRequest(CameraDevice camera, Surface surface) throws CameraAccessException {
        CaptureRequest.Builder captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequest.addTarget(surface);
        CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    // 开始预览，即一直发送预览的请求
                    session.setRepeatingRequest(captureRequest.build(), null, mHandler);

                } catch (CameraAccessException e) {
                    toast("request failed");
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.e(TAG, "ConfigureFailed. session: mCaptureSession");
            }
        };

        // handle 传入 null 表示使用当前线程的 Looper
        camera.createCaptureSession(Collections.singletonList(surface), stateCallback, mHandler);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
