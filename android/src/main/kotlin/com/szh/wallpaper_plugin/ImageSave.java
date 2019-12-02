package com.szh.wallpaper_plugin;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public class ImageSave {

    public static boolean saveImageToFile(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 60, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
