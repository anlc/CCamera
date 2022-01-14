package com.c.sample.saver;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageSaver implements MediaSaver{
    private static final String TAG = "ImageSaver";

    @Override
    public void saveImage(Context context, byte[] jpeg, String path, String name) throws IOException {
        File outFile = new File(path, name);
        Log.d(TAG, "saveImage. filepath: " + outFile.getAbsolutePath());
        try (FileOutputStream os = new FileOutputStream(outFile)) {
            os.write(jpeg);
            os.flush();
            os.close();
            insertToDB(context, outFile.getAbsolutePath());
            Log.i(TAG, "saveImage success");
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
}
