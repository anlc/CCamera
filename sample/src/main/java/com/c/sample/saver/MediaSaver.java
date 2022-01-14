package com.c.sample.saver;

import android.content.Context;

import java.io.IOException;

public interface MediaSaver {
    void saveImage(Context context, byte[] jpeg, String path, String name) throws IOException;
}
