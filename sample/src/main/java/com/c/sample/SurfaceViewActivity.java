package com.c.sample;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * preview : SurfaceView & Camera2
 */
public class SurfaceViewActivity extends AppCompatActivity {

    private static final String TAG = "SurfaceViewActivity";

    private Handler mHandler;
    private CameraManager mCameraManager;
    private HandlerThread mCameraThread;

    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private ImageReader mImageReader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        ImageView thumbnailImage = findViewById(R.id.iv_thumbnail);
        mCameraId = String.valueOf(CameraCharacteristics.LENS_FACING_BACK);
        try {
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            Size largest =Collections.max(Arrays.asList(outputSizes), (size, t1) -> Long.signum((long) size.getWidth() * size.getHeight() -
                    (long) t1.getWidth() * t1.getHeight()));

            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener((ImageReader.OnImageAvailableListener) imageReader -> {
                Image image = imageReader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                thumbnailImage.setImageBitmap(bitmap);
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        findViewById(R.id.iv_shutter).setOnClickListener(view -> {
            if (mCameraCaptureSession != null) {
                try {
                    CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    captureBuilder.addTarget(mImageReader.getSurface());
                    mCameraCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                            Log.i(TAG, "onCaptureStarted: " + session);
                        }
                    }, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        Log.i(TAG, "onCreate: " + mCameraManager + ", " + cameraManager);
        mCameraThread = new HandlerThread("camera");
        mCameraThread.start();
        mHandler = new Handler(mCameraThread.getLooper());

        SurfaceView mSvPreview = findViewById(R.id.sv_preview);
        SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    openCamera(holder);
                } catch (CameraAccessException e) {
                    toast("camera open failed");
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        };
        mSvPreview.getHolder().addCallback(callback);
    }

    @SuppressLint("MissingPermission")
    private void openCamera(SurfaceHolder holder) throws CameraAccessException {
        CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.i(TAG, "onOpened: ");
                try {
                    mCameraDevice = camera;
                    initPreviewRequest(mCameraDevice, holder);
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
        mCameraManager.openCamera(mCameraId, stateCallback, mHandler);
    }

    private void initPreviewRequest(CameraDevice camera, SurfaceHolder holder) throws CameraAccessException {
        CaptureRequest.Builder captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequest.addTarget(holder.getSurface());
        CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    // 开始预览，即一直发送预览的请求
                    mCameraCaptureSession = session;
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
        camera.createCaptureSession(Arrays.asList(holder.getSurface(), mImageReader.getSurface()), stateCallback, mHandler);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
