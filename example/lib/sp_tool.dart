import 'package:shared_preferences/shared_preferences.dart';

class SpTool {
  static SharedPreferences prefs;

  static setString(String key, String value) async {
    if (prefs == null) {
      prefs = await SharedPreferences.getInstance();
    }
    await prefs.setString(key, value);
  }

  static Future<String> getString(String key, String defaultValue) async {
    if (prefs == null) {
      prefs = await SharedPreferences.getInstance();
    }
    String value = prefs.getString(key);
    return value == null ? defaultValue : value;
  }

  static setInt(String key, int value) async {
    if (prefs == null) {
      prefs = await SharedPreferences.getInstance();
    }
    await prefs.setInt(key, value);
  }

  static Future<int> getInt(String key, int defaultValue) async {
    if (prefs == null) {
      prefs = await SharedPreferences.getInstance();
    }
    int value = prefs.getInt(key);
    return value == null ? defaultValue : value;
  }

  static setBool(String key, bool value) async {
    if (prefs == null) {
      prefs = await SharedPreferences.getInstance();
    }
    await prefs.setBool(key, value);
  }

  static Future<bool> getBool(String key, bool defaultValue) async {
    if (prefs == null) {
      prefs = await SharedPreferences.getInstance();
    }
    bool value = prefs.getBool(key);
    return value == null ? defaultValue : value;
  }
}
