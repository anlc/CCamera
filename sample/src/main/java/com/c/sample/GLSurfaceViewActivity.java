package com.c.sample;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collections;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceViewActivity extends AppCompatActivity {

    private static final String TAG = "GlSurfaceViewActivity";

    private Handler mHandler;
    private CameraManager mCameraManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_surface);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        HandlerThread camera = new HandlerThread("camera");
        camera.start();
        mHandler = new Handler(camera.getLooper());

        GLSurfaceView mGlPreview = findViewById(R.id.gl_sf_preview);

        mGlPreview.setEGLContextClientVersion(2);
        GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {

            private SurfaceTexture mSurfaceTexture;
            private CameraDrawer mDrawer;
            private int mTextureId;

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mTextureId = getExternalOESTextureID();
                mSurfaceTexture = new SurfaceTexture(mTextureId);
                mDrawer = new CameraDrawer();
                mSurfaceTexture.setOnFrameAvailableListener(surfaceTexture -> mGlPreview.requestRender());
                try {
                    openCamera(new Surface(mSurfaceTexture));
                } catch (CameraAccessException e) {
                    toast("camera open failed");
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClearColor(0, 0, 0, 0);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                mSurfaceTexture.updateTexImage();
                mDrawer.draw(mTextureId, true);
            }
        };
        mGlPreview.setRenderer(renderer);
        mGlPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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

    public static int getExternalOESTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    public static int loadShader(int type, String source) {
        // 1. create shader
        int shader = GLES20.glCreateShader(type);
        if (shader == GLES20.GL_NONE) {
            Log.e(TAG, "create shared failed! type: " + type);
            return GLES20.GL_NONE;
        }
        // 2. load shader source
        GLES20.glShaderSource(shader, source);
        // 3. compile shared source
        GLES20.glCompileShader(shader);
        // 4. check compile status
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == GLES20.GL_FALSE) { // compile failed
            Log.e(TAG, "Error compiling shader. type: " + type + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader); // delete shader
            shader = GLES20.GL_NONE;
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        // 1. load shader
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == GLES20.GL_NONE) {
            Log.e(TAG, "load vertex shader failed! ");
            return GLES20.GL_NONE;
        }
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == GLES20.GL_NONE) {
            Log.e(TAG, "load fragment shader failed! ");
            return GLES20.GL_NONE;
        }
        // 2. create gl program
        int program = GLES20.glCreateProgram();
        if (program == GLES20.GL_NONE) {
            Log.e(TAG, "create program failed! ");
            return GLES20.GL_NONE;
        }
        // 3. attach shader
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        // we can delete shader after attach
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        // 4. link program
        GLES20.glLinkProgram(program);
        // 5. check link status
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES20.GL_FALSE) { // link failed
            Log.e(TAG, "Error link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program); // delete program
            return GLES20.GL_NONE;
        }
        return program;
    }

    public static class CameraDrawer {

        private final String VERTEX_SHADER = "" +
                "attribute vec4 vPosition;" +
                "attribute vec2 inputTextureCoordinate;" +
                "varying vec2 textureCoordinate;" +
                "void main()" +
                "{" +
                "gl_Position = vPosition;" +
                "textureCoordinate = inputTextureCoordinate;" +
                "}";
        private final String FRAGMENT_SHADER = "" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 textureCoordinate;\n" +
                "uniform samplerExternalOES s_texture;\n" +
                "void main() {" +
                "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                "}";

        private FloatBuffer mVertexBuffer;
        private FloatBuffer mBackTextureBuffer;
        private FloatBuffer mFrontTextureBuffer;
        private ByteBuffer mDrawListBuffer;
        private int mProgram;
        private int mPositionHandle;
        private int mTextureHandle;

        private static final float VERTEXES[] = {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f,
        };

        // 后置摄像头使用的纹理坐标
        private static final float TEXTURE_BACK[] = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f,
        };

        // 前置摄像头使用的纹理坐标
        private static final float TEXTURE_FRONT[] = {
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };

        private static final byte VERTEX_ORDER[] = {0, 1, 2, 3}; // order to draw vertices

        private final int VERTEX_SIZE = 2;
        private final int VERTEX_STRIDE = VERTEX_SIZE * 4;

        public CameraDrawer() {
            // init float buffer for vertex coordinates
            mVertexBuffer = ByteBuffer.allocateDirect(VERTEXES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            mVertexBuffer.put(VERTEXES).position(0);

            // init float buffer for texture coordinates
            mBackTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_BACK.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            mBackTextureBuffer.put(TEXTURE_BACK).position(0);
            mFrontTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_FRONT.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            mFrontTextureBuffer.put(TEXTURE_FRONT).position(0);

            // init byte buffer for draw list
            mDrawListBuffer = ByteBuffer.allocateDirect(VERTEX_ORDER.length).order(ByteOrder.nativeOrder());
            mDrawListBuffer.put(VERTEX_ORDER).position(0);

            mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mTextureHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        }

        public void draw(int texture, boolean isFrontCamera) {
            GLES20.glUseProgram(mProgram); // 指定使用的program
            GLES20.glEnable(GLES20.GL_CULL_FACE); // 启动剔除
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture); // 绑定纹理
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

            GLES20.glEnableVertexAttribArray(mTextureHandle);
            if (isFrontCamera) {
                GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mFrontTextureBuffer);
            } else {
                GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mBackTextureBuffer);
            }
            // 真正绘制的操作
            // GL_TRIANGLE_FAN模式，绘制 (0, 1, 2) 和 (0, 2, 3) 两个三角形
            // glDrawElements绘制索引，索引0为VERTEXES数组第一个点 (-1, 1)，以此类推
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_BYTE, mDrawListBuffer);

            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mTextureHandle);
        }
    }
}
