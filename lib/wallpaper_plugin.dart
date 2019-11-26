import 'dart:async';

import 'package:flutter/services.dart';

class WallpaperPlugin {
  static const MethodChannel _channel = const MethodChannel('wallpaper_plugin');

  static Future<bool> setSystemWallpaper(String path) async {
    final Map<String, dynamic> params = <String, dynamic>{'path': path};
    final bool isSuccess =
        await _channel.invokeMethod('SystemWallpaper', params);
    return isSuccess;
  }

  static Future<bool> setVideoWallpaper(String path, bool volume) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'path': path,
      'volume': volume
    };
    final bool isSuccess =
        await _channel.invokeMethod('VideoWallpaper', params);
    return isSuccess;
  }

  static Future<bool> setWallPaperDirect(String path) async {
    final Map<String, dynamic> params = <String, dynamic>{'path': path};
    final bool isSuccess =
        await _channel.invokeMethod('WallPaperDirect', params);
    return isSuccess;
  }
}
