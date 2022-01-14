package com.c.sample.camera;

import static android.content.Context.CAMERA_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;

public class CameraProxy {

    private static final String TAG = "CameraProxy";

    private Handler mHandler;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;

    public CameraProxy(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        HandlerThread camera = new HandlerThread("camera");
        camera.start();
        mHandler = new Handler(camera.getLooper());
    }

    public void closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    @SuppressLint("MissingPermission")
    public void openCamera(String cameraId, Surface surface) throws CameraAccessException {
        CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.i(TAG, "onOpened: ");
                try {
                    mCameraDevice = camera;
                    initPreviewRequest(camera, surface);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
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

        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] outputSizes = configurationMap.getOutputSizes(ImageFormat.JPEG);
        Size max = Collections.max(
                Arrays.asList(outputSizes),
                (o1, o2) -> Long.signum((long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight())
        );
        mImageReader = ImageReader.newInstance(max.getWidth(), max.getHeight(), ImageFormat.JPEG, 2);
        mCameraManager.openCamera(cameraId, stateCallback, mHandler);
    }

    private void initPreviewRequest(CameraDevice camera, Surface surface) throws CameraAccessException {
        CaptureRequest.Builder captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequest.addTarget(surface);
        CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    mCameraCaptureSession = session;
                    // 开始预览，即一直发送预览的请求
                    session.setRepeatingRequest(captureRequest.build(), null, mHandler);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.e(TAG, "ConfigureFailed. session: mCaptureSession");
            }
        };

        // handle 传入 null 表示使用当前线程的 Looper
        camera.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), stateCallback, mHandler);
    }

    public void setOnImageAvailableListener(ImageReader.OnImageAvailableListener listener) {
        if (mImageReader == null) {
            throw new IllegalStateException("init error");
        }
        mImageReader.setOnImageAvailableListener(listener, mHandler);
    }

    public void capture() throws CameraAccessException, IllegalStateException {
        if (mCameraDevice == null || mCameraCaptureSession == null) {
            throw new IllegalStateException("init error");
        }
        CaptureRequest.Builder request = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        request.addTarget(mImageReader.getSurface());
        mCameraCaptureSession.capture(request.build(), new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Log.i(TAG, "onCaptureCompleted: === ");
            }
        }, mHandler);
    }
}
