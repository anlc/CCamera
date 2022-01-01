package com.c.sample;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private CameraProxy mCameraProxy;
    private String mCameraId;
    private String mPicSavePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);

        mCameraProxy = new CameraProxy(this);
        mCameraId = String.valueOf(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE);
        mPicSavePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();

        SurfaceView mSvPreview = findViewById(R.id.sv_preview);
        SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    mCameraProxy.openCamera(mCameraId, holder.getSurface());
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
                mCameraProxy.closeCamera();
            }
        };
        mSvPreview.getHolder().addCallback(callback);

        findViewById(R.id.iv_shutter).setOnClickListener(v -> {
            try {
                mCameraProxy.setOnImageAvailableListener(reader -> saveImage(reader.acquireNextImage()));
                mCameraProxy.capture();
            } catch (CameraAccessException e) {
                toast("capture error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void saveImage(Image image) {
        new Thread(() -> {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            image.close();
            try {
                saveImage(CameraActivity.this, bytes, mPicSavePath, System.currentTimeMillis() + ".jpg");
            } catch (IOException e) {
                toast("save image error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public static void saveImage(Context context, byte[] jpeg, String path, String name) throws IOException {
        File outFile = new File(path, name);
        Log.d(TAG, "saveImage. filepath: " + outFile.getAbsolutePath());
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
//        Bitmap cacheBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap cacheBitmap = Bitmap.createBitmap(bitmap);
        Canvas canvas = new Canvas(cacheBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics()));
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawText("xxxxxxxxxxxxxxxxx", 100, 100, paint);
        canvas.save();
        canvas.restore();
        try (FileOutputStream os = new FileOutputStream(outFile)) {
            boolean compress = cacheBitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
            Log.i(TAG, "saveImage: " + compress);
//            os.write(jpeg);
            os.flush();
            os.close();
            insertToDB(context, outFile.getAbsolutePath());
        }
    }

    public static void insertToDB(Context context, String picturePath) {
        ContentValues values = new ContentValues();
        ContentResolver resolver = context.getContentResolver();
        values.put(MediaStore.Images.ImageColumns.DATA, picturePath);
        values.put(MediaStore.Images.ImageColumns.TITLE, picturePath.substring(picturePath.lastIndexOf("/") + 1));
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static class CameraProxy {

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
        private void openCamera(String cameraId, Surface surface) throws CameraAccessException {
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
}
