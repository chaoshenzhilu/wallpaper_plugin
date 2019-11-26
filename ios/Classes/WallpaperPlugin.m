#import "WallpaperPlugin.h"
#import <wallpaper_plugin/wallpaper_plugin-Swift.h>

@implementation WallpaperPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftWallpaperPlugin registerWithRegistrar:registrar];
}
@end
