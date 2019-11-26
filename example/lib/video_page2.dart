import 'dart:io';
import 'dart:isolate';
import 'dart:ui';
import 'package:wallpaper_plugin/wallpaper_plugin.dart';
import 'package:flutter/material.dart';
import 'package:flutter_downloader/flutter_downloader.dart';
import 'package:path_provider/path_provider.dart';
import 'package:wallpaper_plugin_example/sp_tool.dart';
import 'package:wallpaper_plugin_example/toast_tool.dart';

class VideoPage2 extends StatefulWidget {
  @override
  _VideoPage2State createState() => _VideoPage2State();
}

class _VideoPage2State extends State<VideoPage2> {
  int status = 0; //0未下载，1下载中，2已下载；
  int progress = 0;
  String tempPath;
  String appDocPath;
  ReceivePort _port = ReceivePort();
  String fileName;
  String videoUrl;
  String taskId;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    videoUrl =
        'http://up1.bdcdn.bizhiduoduo.com//vd2//190412//hy_6d4a39638d323fd8a345962bbf118978.mp4';
    fileName = '5479528.mp4';
    _init();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('视频壁纸'),
      ),
      body: Column(
        children: <Widget>[
          MaterialButton(
            onPressed: () async {
              if (status == 0) {
                _downVideo();
              } else if (status == 2) {
                bool isSuccess = await WallpaperPlugin.setVideoWallpaper(
                    appDocPath + "/" + fileName, true);
                if (isSuccess) {
                  ToastTool.show('设置成功');
                } else {
                  ToastTool.show('设置失败');
                }
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
      str = '下载视频';
    } else if (status == 1) {
      str = '$progress%';
    } else {
      str = '设置视频壁纸';
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
        taskId = await SpTool.getString(videoUrl, '');
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
      url: videoUrl,
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
}
