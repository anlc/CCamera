package com.c.sample.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.c.sample.R;
import com.c.sample.saver.ImageSaver;
import com.c.sample.saver.MediaSaver;
import com.c.sample.saver.WatermarkSaver;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraActivity extends AppCompatActivity {

    private CameraProxy mCameraProxy;
    private String mCameraId;
    private String mPicSavePath;
    private MediaSaver mSaver;

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

        mSaver = new WatermarkSaver(new ImageSaver());
    }

    private void saveImage(Image image) {
        new Thread(() -> {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            image.close();
            try {
                mSaver.saveImage(CameraActivity.this, bytes, mPicSavePath, System.currentTimeMillis() + ".jpg");
            } catch (IOException e) {
                toast("save image error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
