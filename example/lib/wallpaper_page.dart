import 'dart:convert';
import 'dart:io';
import 'dart:isolate';
import 'dart:ui';
import 'package:crypto/crypto.dart';
import 'package:wallpaper_plugin/wallpaper_plugin.dart';
import 'package:flutter/material.dart';
import 'package:flutter_downloader/flutter_downloader.dart';
import 'package:path_provider/path_provider.dart';
import 'package:wallpaper_plugin_example/sp_tool.dart';
import 'package:wallpaper_plugin_example/toast_tool.dart';

class WallpaperPage extends StatefulWidget {
  @override
  _WallpaperPageState createState() => _WallpaperPageState();
}

class _WallpaperPageState extends State<WallpaperPage> {
  int status = 0; //0未下载，1下载中，2已下载；
  int progress = 0;
  String tempPath;
  String appDocPath;
  ReceivePort _port = ReceivePort();
  String fileName;
  String imageUrl;

  String taskId;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    imageUrl =
        'http://material_manager.lionmobi.com/public/upload/285eab34.png';
    fileName = generateMd5(imageUrl) + '.png';
    _init();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('静态壁纸'),
      ),
      body: Column(
        children: <Widget>[
          MaterialButton(
            onPressed: () {
              if (status == 0) {
                _downVideo();
              } else if (status == 2) {
                showDialog<Null>(
                  context: context,
                  builder: (BuildContext context) {
                    return new SimpleDialog(
                      title: new Text('选择方式'),
                      children: <Widget>[
                        new SimpleDialogOption(
                          child: new Text('通过系统设置'),
                          onPressed: () async {
                            Navigator.pop(context);
                            bool isSuccess =
                                await WallpaperPlugin.setSystemWallpaper(
                                    appDocPath + "/" + fileName);
                            if (isSuccess) {
                              ToastTool.show('设置成功');
                            } else {
                              ToastTool.show('设置失败');
                            }
                          },
                        ),
                        new SimpleDialogOption(
                          child: new Text('直接设置'),
                          onPressed: () async {
                            Navigator.pop(context);
                            bool isSuccess =
                                await WallpaperPlugin.setWallPaperDirect(
                                    appDocPath + "/" + fileName);
                            if (isSuccess) {
                              ToastTool.show('设置成功');
                            } else {
                              ToastTool.show('设置失败');
                            }
                          },
                        ),
                      ],
                    );
                  },
                ).then((val) {
                  print(val);
                });
              }
            },
            child: Text(getText()),
          ),
        ],
      ),
    );
  }

  String getText() {
    String str;
    if (status == 0) {
      str = '下载壁纸';
    } else if (status == 1) {
      str = '$progress%';
    } else {
      str = '设置壁纸';
    }
    return str;
  }

  Future<void> _init() async {
    localPath();
    IsolateNameServer.registerPortWithName(
        _port.sendPort, 'downloader_send_port');
    _port.listen((dynamic data) {
      String id = data[0];
      DownloadTaskStatus status = data[1];
      int progress = data[2];
      if (status == DownloadTaskStatus.enqueued) {
        print('开始下载');
      }
      if (status == DownloadTaskStatus.failed) {
        print('下载异常，请稍后重试');
      }
      if (status == DownloadTaskStatus.complete) {
        print('下载完成');
        setState(() {
          this.status = 2;
        });
      }
      if (status == DownloadTaskStatus.running) {
        print("下载进度" + progress.toString());
        setState(() {
          setState(() {
            this.status = 1;
            this.progress = progress;
          });
        });
      }
    });
    FlutterDownloader.registerCallback(downloadCallback);
  }

  static void downloadCallback(
      String id, DownloadTaskStatus status, int progress) {
    print(
        'Background Isolate Callback: task ($id) is in status ($status) and process ($progress)');
    final SendPort send =
        IsolateNameServer.lookupPortByName('downloader_send_port');
    send.send([id, status, progress]);
  }

  localPath() async {
    try {
      var tempDir = await getTemporaryDirectory();
      tempPath = tempDir.path;
      //Android 存放在sd卡 ios放在文档目录
      if (Theme.of(context).platform == TargetPlatform.android) {
        var StorageDirectory = await getExternalStorageDirectory();
        appDocPath = StorageDirectory.path;
      } else {
        var appDocDir = await getApplicationDocumentsDirectory();
        appDocPath = appDocDir.path;
      }
      print('临时目录::' + tempPath);
      print('文档目录::' + appDocPath);
      File file = new File(appDocPath + '/' + fileName);
      if (await file.exists()) {
        taskId = await SpTool.getString(imageUrl, '');
        print('taskId::' + taskId);
        setState(() {
          status = 2;
        });
      }
    } catch (err) {
      print(err);
    }
  }

  Future<void> _downVideo() async {
    final taskId = await FlutterDownloader.enqueue(
      url: imageUrl,
      savedDir: appDocPath,
      fileName: fileName,
      showNotification: false,
      // show download progress in status bar (for Android)
      openFileFromNotification:
          true, // click on notification to open downloaded file (for Android)
    );
    this.taskId = taskId;
    print('taskId2::' + taskId);
    SpTool.setString(fileName, taskId);
  }

  @override
  void dispose() {
    // TODO: implement dispose
    IsolateNameServer.removePortNameMapping('downloader_send_port');
    super.dispose();
  }

  // md5 加密
  String generateMd5(String data) {
    var content = new Utf8Encoder().convert(data);
    var digest = md5.convert(content);
    // 这里其实就是 digest.toString()
    return digest.toString();
  }
}
