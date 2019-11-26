package com.szh.wallpaper_plugin_example
import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback
import io.flutter.plugins.GeneratedPluginRegistrant

class MyApplication : FlutterApplication(), PluginRegistrantCallback {
    override fun registerWith(registry: PluginRegistry) { //
        GeneratedPluginRegistrant.registerWith(registry)
    }
}