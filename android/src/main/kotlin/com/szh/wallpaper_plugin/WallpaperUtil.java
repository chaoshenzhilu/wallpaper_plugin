package com.szh.wallpaper_plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.IOException;


/**
 *
 */
public class WallpaperUtil {


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
            wallpaperManager.setBitmap(bitmap);
            // 已过时的Api
//            setWallpaper(wallpaperBitmap);
//            setWallpaper(getResources().openRawResource(R.raw.girl));
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

            // 已过时的Api
//            clearWallpaper();
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
}
