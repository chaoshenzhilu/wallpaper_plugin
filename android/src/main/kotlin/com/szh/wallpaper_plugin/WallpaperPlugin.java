package com.szh.wallpaper_plugin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class WallpaperPlugin implements MethodChannel.MethodCallHandler {
    private final static int REQUEST_CODE_VIDEO_WALLPAPER = 0x001;
    private final static int REQUEST_CODE_SELECT_SYSTEM_WALLPAPER = 0x002;
    private static String Tag = "WallpaperPlugin";
    private Activity activity;
    MethodChannel.Result result;
    PluginRegistry.Registrar registrar;

    private WallpaperPlugin(PluginRegistry.Registrar registrar) {
        this.registrar = registrar;
        this.activity = registrar.activity();
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(PluginRegistry.Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "wallpaper_plugin");
        final WallpaperPlugin wallpaperPlugin = new WallpaperPlugin(registrar);
        channel.setMethodCallHandler(wallpaperPlugin);
        registrar.addActivityResultListener(new PluginRegistry.ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
                wallpaperPlugin.handleActivityResult(requestCode, resultCode, intent);
                return false;
            }
        });
    }

    private void handleActivityResult(int requestCode, int resultCode, Intent intent) {
        if (result == null) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            result.success(true);
        } else {
            result.success(false);
        }
        result = null;
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        this.result = result;
        String path = call.argument("path").toString();
        switch (call.method) {
            case "WallPaperDirect":
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                result.success(WallpaperUtil.onSetWallpaperForBitmap(activity, bitmap));
                break;
            case "SystemWallpaper":
//                setWallpaperByIntent(path, result);
//                setWallpaper(path, result);
                break;
            case "VideoWallpaper":
                boolean volume = call.argument("volume");
                LiveWallpaperService.startWallPaper(activity, path, volume, REQUEST_CODE_VIDEO_WALLPAPER);
                break;
            case "SetLockWallPaper":
                result.success(WallpaperUtil.setLockWallPaper(activity, path));
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    @SuppressLint("MissingPermission")
    private void setWallpaper(String path, MethodChannel.Result result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED &&
                    activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                File file = new File(path);
                Uri contentURI = getImageContentUri(activity, file);
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
                Intent intent = new Intent(wallpaperManager.getCropAndSetWallpaperIntent(contentURI));
                String mime = "image/png";
                intent.setDataAndType(contentURI, mime);
                try {
                    Intent chooser = Intent.createChooser(intent, "请选择操作");
                    activity.startActivityForResult(chooser, REQUEST_CODE_SELECT_SYSTEM_WALLPAPER);
                } catch (ActivityNotFoundException e) {
                }
            }
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            result.success(WallpaperUtil.onSetWallpaperForBitmap(activity, bitmap));
        }
    }

    void setWallpaperByIntent(String path, MethodChannel.Result result) {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mimeType", "image/*");
        try {
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(activity.getContentResolver(), path, null, null));
            intent.setData(uri);
            activity.startActivityForResult(intent, REQUEST_CODE_SELECT_SYSTEM_WALLPAPER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result.success(false);
        }
    }

    private static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Log.d("Tag", filePath);
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}