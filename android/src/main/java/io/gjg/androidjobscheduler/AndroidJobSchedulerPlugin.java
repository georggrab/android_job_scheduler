package io.gjg.androidjobscheduler;

import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * AndroidJobSchedulerPlugin
 */
public class AndroidJobSchedulerPlugin implements MethodCallHandler {
  private Context mContext;

  public AndroidJobSchedulerPlugin(Context context) {
    this.mContext = context;
  }
  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "plugins.gjg.io/android_job_scheduler");
    AndroidJobScheduler.callbackMethodChannel = channel;
    channel.setMethodCallHandler(new AndroidJobSchedulerPlugin(registrar.context()));
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("scheduleEvery")) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final ArrayList<?> args = (ArrayList<?>) call.arguments;
            final Integer every = (Integer) args.get(0);
            final String funcCallback = (String) args.get(1);
            AndroidJobScheduler.scheduleEvery(this.mContext, every, funcCallback);
            result.success(true);
        } else {
            result.success(false);
        }
    } else {
      result.notImplemented();
    }
  }
}
