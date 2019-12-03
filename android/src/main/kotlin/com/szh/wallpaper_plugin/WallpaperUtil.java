package com.szh.wallpaper_plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;

import io.flutter.Log;


/**
 *
 */
public class WallpaperUtil {
    public static String TAG = "WallpaperUtil";

    /**
     * 使用资源文件设置壁纸
     * 直接设置为壁纸，不会有任何界面和弹窗出现
     */
    @SuppressLint({"MissingPermission", "NewApi"})
    public static void onSetWallpaperForResource(Activity activity, int raw) {
//        WallpaperManager manager =(WallpaperManager)getSystemService(WALLPAPER_SERVICE);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
        try {
            wallpaperManager.setResource(raw);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////                WallpaperManager.FLAG_LOCK WallpaperManager.FLAG_SYSTEM
////                wallpaperManager.setResource(R.raw.wallpaper, WallpaperManager.FLAG_SYSTEM);
////            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用Bitmap设置壁纸
     * 直接设置为壁纸，不会有任何界面和弹窗出现
     * 壁纸切换，会有动态的渐变切换
     */
    @SuppressLint({"MissingPermission", "NewApi"})
    public static boolean onSetWallpaperForBitmap(Activity activity, Bitmap bitmap) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
        try {
            // 1. 设置WallpaperManager适应屏幕尺寸
            final DisplayMetrics metrics = setWallpaperManagerFitScreen(activity);
            // 2. center-crop裁剪
            Bitmap wallpaper = centerCrop(bitmap, metrics);
            wallpaperManager.setBitmap(wallpaper);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 清除壁纸
     */
    @SuppressLint({"MissingPermission", "NewApi"})
    public static void clearWallpaper(Activity activity) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
        try {
            wallpaperManager.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否是使用我们的壁纸
     *
     * @param paramContext
     * @return
     */
    @SuppressLint("NewApi")
    public static boolean wallpaperIsUsed(Context paramContext) {
        WallpaperInfo localWallpaperInfo = null;
        localWallpaperInfo = WallpaperManager.getInstance(paramContext).getWallpaperInfo();
        return ((localWallpaperInfo != null) && (localWallpaperInfo.getPackageName().equals(paramContext.getPackageName())) &&
                (localWallpaperInfo.getServiceName().equals(LiveWallpaperService.class.getCanonicalName())));
    }

    @SuppressLint("NewApi")
    public static Bitmap getDefaultWallpaper(Context paramContext) {
        Bitmap localBitmap;
        if (isLivingWallpaper(paramContext))
            localBitmap = null;
        do {
            localBitmap = ((BitmapDrawable) WallpaperManager.getInstance(paramContext).getDrawable()).getBitmap();
            return localBitmap;
        }
        while (localBitmap != null);
    }

    @SuppressLint("NewApi")
    public static boolean isLivingWallpaper(Context paramContext) {
        return (WallpaperManager.getInstance(paramContext).getWallpaperInfo() != null);
    }

    @SuppressLint({"NewApi", "MissingPermission"})
    public static boolean setLockWallPaper(Activity activity, String filePath) {
        // TODO Auto-generated method stub
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
        try {
            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("NewApi")
    public static DisplayMetrics setWallpaperManagerFitScreen(Activity context) {
        // 使桌面适应屏幕尺寸
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = context.getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        final int screenWidth  = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        wallpaperManager.suggestDesiredDimensions(screenWidth, screenHeight);

        // 获取壁纸硬设尺寸
        DisplayMetrics ret = new DisplayMetrics();
        ret.widthPixels = wallpaperManager.getDesiredMinimumWidth();
        ret.heightPixels = wallpaperManager.getDesiredMinimumHeight();
        return ret;
    }


    public static Bitmap centerCrop(Bitmap bitmap, DisplayMetrics screenMetrics) {
        Bitmap containScreen = scaleBitmapToContainScreen(bitmap, screenMetrics);
        return cropCenter(containScreen, screenMetrics);
    }

    /**
     * 将bitmap放大到包含屏幕尺寸的大小。
     * @param bitmap 要放大的图片
     * @param screenMetrics 屏幕的尺寸
     * @return 放大后的图片
     */
    private static Bitmap scaleBitmapToContainScreen(Bitmap bitmap, DisplayMetrics screenMetrics) {
        int height = screenMetrics.heightPixels;
        int width = screenMetrics.widthPixels;

        double wallpaperScale = (double)bitmap.getHeight() / (double)bitmap.getWidth();
        double screenScale = (double) height / (double) width;
        int targetWidth;
        int targetHeight;
        if (wallpaperScale < screenScale) {
            targetHeight = height;
            targetWidth = (int)(targetHeight / wallpaperScale);
        } else {
            targetWidth = width;
            targetHeight = (int)(targetWidth * wallpaperScale);
        }
        return  Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }

    /**
     * 对刚好包含屏幕的图片进行中心裁剪。
     * @param bitmap 宽或高刚好包含屏幕的图片
     * @param screenMetrics 屏幕的尺寸
     * @return 若高的部分多余，裁剪掉上下两边多余部分并返回。
     * 若宽的部分多余，裁减掉左右两边多于部分并返回。
     */
    private static Bitmap cropCenter(Bitmap bitmap, DisplayMetrics screenMetrics) {
        int h1 = bitmap.getHeight();
        int w1 = bitmap.getWidth();
        int h2 = screenMetrics.heightPixels;
        int w2 = screenMetrics.widthPixels;

        if (w1 > w2){
            return Bitmap.createBitmap(bitmap, (w1 - w2) / 2, 0, w2, h2);
        }else{
            return Bitmap.createBitmap(bitmap, 0, (h1 - h2) / 2, w2, h2);
        }
    }
}
