package com.c.sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                finish();
            }
        }).launch(Manifest.permission.CAMERA);

        findViewById(R.id.btn_surface_view).setOnClickListener(v -> startActivity(SurfaceViewActivity.class));
        findViewById(R.id.btn_texture_view).setOnClickListener(v -> startActivity(TextureViewActivity.class));
        findViewById(R.id.btn_gl_surface_view).setOnClickListener(v -> startActivity(GLSurfaceViewActivity.class));
        findViewById(R.id.btn_camera).setOnClickListener(v -> startActivity(CameraActivity.class));
    }

    private void startActivity(Class<?> cls) {
        startActivity(new Intent(MainActivity.this, cls));
    }
}