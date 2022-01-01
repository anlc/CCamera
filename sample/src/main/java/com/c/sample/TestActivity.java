package com.c.sample;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imageView = new ImageView(this);
        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setTextSize(50);
        paint.setColor(Color.RED);

        int startX = 100;
        int startY = 200;
        canvas.drawLine(startX, startY, startX + 300, startY, paint);
        canvas.drawText("xxxx", startX, startY, paint);
        imageView.setImageBitmap(bitmap);
        setContentView(imageView);
    }

    public Bitmap getTimestampBitmap(){
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

    public static Paint getWatermarkTextPaint(int textSize){
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
