package io.gjg.androidjobscheduler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

import io.flutter.app.FlutterApplication;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;

@TargetApi(Build.VERSION_CODES.M)
public class AndroidJobScheduler extends JobService {
    public static MethodChannel callbackMethodChannel;
    private static PluginRegistry.PluginRegistrantCallback pluginRegistrantCallback;
    private static String TAG = AndroidJobScheduler.class.getSimpleName();
    private static String B_KEY_RESCHEDULE = "reschedule";
    private static String B_KEY_INTERVAL = "interval";
    private static String B_KEY_DART_CB = "callback";
    private static String B_KEY_ID = "id";

    public static void scheduleEvery(Context context, Integer millis, String callback, Integer id) {
        JobInfo info;
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(B_KEY_DART_CB, callback);
        bundle.putInt(B_KEY_INTERVAL, millis);
        bundle.putInt(B_KEY_ID, id);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            bundle.putBoolean(B_KEY_RESCHEDULE, false);
            info = new JobInfo.Builder(id, new ComponentName(context, AndroidJobScheduler.class))
                    .setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setPeriodic(millis)
                    .setExtras(bundle)
                    .build();
        } else {
            bundle.putBoolean(B_KEY_RESCHEDULE, true);
            info = new JobInfo.Builder(id, new ComponentName(context, AndroidJobScheduler.class))
                    .setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setMinimumLatency(millis)
                    .setExtras(bundle)
                    .build();
        }
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.schedule(info);
    }

    public static void cancelJob(Context context, Integer jobId) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.cancel(jobId);
    }

    public static void cancelAllJobs(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.cancelAll();
    }

    public static void setPluginRegistrantCallback(PluginRegistry.PluginRegistrantCallback callback) {
        AndroidJobScheduler.pluginRegistrantCallback = callback;
    }

    private static boolean isApplicationRunning(Context context) {
        if (!(context instanceof FlutterApplication)) {
            return false;
        }
        Activity activity = ((FlutterApplication) context).getCurrentActivity();
        return activity != null;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle extras = params.getExtras();
        Context context = getApplicationContext();
        if (isApplicationRunning(context)) {
            if (callbackMethodChannel != null) {
                callbackMethodChannel.invokeMethod("firedWhileApplicationRunning", null);
            }
        } else {
            FlutterNativeView nativeView = new FlutterNativeView(context);
            if (AndroidJobScheduler.pluginRegistrantCallback != null) {
                AndroidJobScheduler.pluginRegistrantCallback.registerWith(nativeView.getPluginRegistry());
            }
            nativeView.runFromBundle(FlutterMain.findAppBundlePath(context), null,
                    extras.getString(B_KEY_DART_CB), true);
        }
        if (extras.getBoolean(B_KEY_RESCHEDULE)) {
            AndroidJobScheduler.scheduleEvery(getApplicationContext(),
                    extras.getInt(B_KEY_INTERVAL),
                    extras.getString(B_KEY_DART_CB),
                    extras.getInt(B_KEY_ID));
        }
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(AndroidJobScheduler.class.getSimpleName(), "onStopJob");
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlutterMain.ensureInitializationComplete(getApplicationContext(), null);
    }
}
