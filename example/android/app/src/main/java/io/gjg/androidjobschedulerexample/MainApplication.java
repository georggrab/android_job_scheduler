package io.gjg.androidjobschedulerexample;


import io.flutter.app.FlutterApplication;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.gjg.androidjobscheduler.AndroidJobScheduler;

public class MainApplication extends FlutterApplication implements PluginRegistry.PluginRegistrantCallback {

    @Override
    public void registerWith(PluginRegistry pluginRegistry) {
        GeneratedPluginRegistrant.registerWith(pluginRegistry);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidJobScheduler.setPluginRegistrantCallback(this);
    }
}