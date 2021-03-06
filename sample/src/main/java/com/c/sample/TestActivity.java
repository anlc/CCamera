package com.c.sample;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResizeView();
    }

    private void setRotateView() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.img);
        Canvas canvas = new Canvas(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        canvas.drawColor(Color.BLACK);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        setContentView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        findViewById(android.R.id.content).setBackgroundColor(Color.BLACK);
    }

    private void setResizeView() {
        Bitmap bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);

        Bitmap dest = Bitmap.createBitmap(80, 50, Bitmap.Config.ARGB_8888);
        Canvas destCanvas = new Canvas(dest);
        destCanvas.drawColor(Color.WHITE);
        destCanvas.drawText("123456", 20, 50, getWatermarkTextPaint(10));


        dest = resizeBitmap(bitmap, dest, 1, 90);

        bitmap = addBitmap(bitmap, dest, 80, 80);

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        setContentView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private Bitmap resizeBitmap(Bitmap srcBitmap, Bitmap bitmap, float scale, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return resultBitmap;
    }

    private Bitmap addBitmap(Bitmap srcBitmap, Bitmap bitmap, int left, int top) {
        Bitmap destBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        canvas.drawBitmap(bitmap, left, top, null);
        canvas.save();
        canvas.restore();
        srcBitmap.recycle();
        bitmap.recycle();
        return destBitmap;
    }

    private void setScaleView() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(this);
//        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint();
//        paint.setTextSize(50);
//        paint.setColor(Color.RED);
//
//        int startX = 100;
//        int startY = 200;
//        canvas.drawLine(startX, startY, startX + 300, startY, paint);
//        canvas.drawText("xxxx", startX, startY, paint);
//        imageView.setImageBitmap(bitmap);
        imageView.setImageBitmap(getAddBitmap(getWatermarkBitmap(), 100, 100));

        ImageView imageView2 = new ImageView(this);
        imageView2.setImageBitmap(getAddBitmap(getWatermarkBitmap(), 1000, 1000));

        linearLayout.addView(imageView);
        linearLayout.addView(imageView2);

        setContentView(linearLayout);
    }

    public Bitmap getAddBitmap(Bitmap watermark, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.RED);
////        Matrix matrix = new Matrix();
////        int sx = bitmap.getWidth() / watermark.getWidth();
////        matrix.postScale(1, 1);
////        canvas.drawBitmap(watermark, matrix, null);
//        Rect rect = new Rect(0, 0, watermark.getWidth(), watermark.getHeight());
//        Rect destRect = new Rect(0, bitmap.getHeight() - watermark.getHeight(), watermark.getWidth(), bitmap.getHeight());
//        canvas.drawBitmap(watermark, rect, destRect, null);

        float sx = ((float) width) / watermark.getWidth() / 10;
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sx);
        Bitmap watermarkDest = Bitmap.createBitmap(watermark, 0, 0, watermark.getWidth(), watermark.getHeight(), matrix, true);
        canvas.drawBitmap(watermarkDest, 0, height - watermarkDest.getHeight(), null);
        return bitmap;
    }

    public Bitmap getWatermarkBitmap() {
        String content = "shot on xx";

        Paint textPaint = getWatermarkTextPaint(16);
        Rect textBounds = new Rect();
        textPaint.getTextBounds(content, 0, content.length(), textBounds);

        Drawable logoDrawable = getDrawable(R.mipmap.ic_launcher);

        int marge = 10;

        Bitmap resultBitmap = Bitmap.createBitmap(logoDrawable.getIntrinsicWidth() + marge + textBounds.width(), logoDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawColor(Color.BLACK);

        logoDrawable.setBounds(0, 0, logoDrawable.getIntrinsicWidth(), logoDrawable.getIntrinsicHeight());
        logoDrawable.draw(canvas);

        canvas.drawText(content, logoDrawable.getIntrinsicWidth() + marge, logoDrawable.getIntrinsicHeight() / 2, textPaint);
        return resultBitmap;
    }

    public Bitmap getTimestampBitmap() {
        String timestampStr = getDateStr(getContentResolver());
        Paint paint = getWatermarkTextPaint(20);
        Rect textBounds = new Rect();
        paint.getTextBounds(timestampStr, 0, timestampStr.length(), textBounds);
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.RED);
        canvas.drawText(timestampStr, 0, textBounds.height(), paint);
        return bitmap;
    }

    public static Paint getWatermarkTextPaint(int textSize) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    public static String getDateStr(ContentResolver mResolver) {
        String timeFormatStr;
        String timeFormat = android.provider.Settings.System.getString(mResolver, android.provider.Settings.System.TIME_12_24);
        if ("24".equals(timeFormat)) {
            timeFormatStr = "MM dd, yyyy, HH:mm";
        } else {
            timeFormatStr = "MM dd, yyyy, h:mm a";
        }
        return new SimpleDateFormat(timeFormatStr).format(new Date());
    }
}
