import Flutter
import UIKit

public class SwiftWallpaperPlugin: NSObject, FlutterPlugin {
  var result: FlutterResult?;
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "wallpaper_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftWallpaperPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    self.result = result
    if call.method == "saveImageToFile" {
      guard let imageData = (call.arguments as? FlutterStandardTypedData)?.data, let image = UIImage(data: imageData) else { return }
      UIImageWriteToSavedPhotosAlbum(image, self, #selector(didFinishSavingImage(image:error:contextInfo:)), nil)
    }else {
        result(FlutterMethodNotImplemented)
    }
  }
  /// finish saving，if has error，parameters error will not nill
  @objc func didFinishSavingImage(image: UIImage, error: NSError?, contextInfo: UnsafeMutableRawPointer?) {
      result?(error == nil)
  }
  @objc func didFinishSavingVideo(videoPath: String, error: NSError?, contextInfo: UnsafeMutableRawPointer?) {
      result?(error == nil)
  }

  func isImageFile(filename: String) -> Bool {
      return filename.hasSuffix(".jpg")
          || filename.hasSuffix(".png")
          || filename.hasSuffix(".JPEG")
          || filename.hasSuffix(".JPG")
          || filename.hasSuffix(".PNG")
  }
}
