package com.c.sample.saver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;

import java.io.IOException;
import java.nio.ByteBuffer;

public class WatermarkSaver implements MediaSaver {

    private MediaSaver mSaver;

    public WatermarkSaver(MediaSaver saver) {
        mSaver = saver;
    }

    @Override
    public void saveImage(Context context, byte[] jpeg, String path, String name) throws IOException {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, context.getResources().getDisplayMetrics()));
        Bitmap srcBitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
        Bitmap destBitmap = Bitmap.createBitmap(srcBitmap);
        Canvas canvas = new Canvas(destBitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        canvas.drawText("watermark", 100, 100, paint);
        canvas.save();
        canvas.restore();
        ByteBuffer byteBuffer = ByteBuffer.allocate(destBitmap.getByteCount());
        destBitmap.copyPixelsToBuffer(byteBuffer);
        mSaver.saveImage(context, byteBuffer.array(), path, name);
    }
}
