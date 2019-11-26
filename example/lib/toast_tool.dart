import 'dart:ui';

import 'package:fluttertoast/fluttertoast.dart';

class ToastTool{
  static show(String msg){
    Fluttertoast.showToast(
        msg: msg,
        toastLength: Toast.LENGTH_SHORT,
        gravity: ToastGravity.BOTTOM,
        timeInSecForIos: 1,
        backgroundColor: Color(0xFF9E9E9E),
        textColor: Color(0xFFffffff));
  }
}