package io.gjg.androidjobscheduler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;

import java.util.List;

import io.flutter.app.FlutterApplication;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;

import static io.gjg.androidjobscheduler.AndroidJobSchedulerUtils.B_KEY_DART_CB;

@TargetApi(Build.VERSION_CODES.M)
public class AndroidJobScheduler extends JobService {
    public static MethodChannel callbackMethodChannel;

    private static PluginRegistry.PluginRegistrantCallback pluginRegistrantCallback;
    private static String TAG = AndroidJobScheduler.class.getSimpleName();

    public static void scheduleEvery(Context context, JobInfo jobInfo) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        scheduler.schedule(jobInfo);
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

    public static List<JobInfo> getAllPendingJobs(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        return scheduler.getAllPendingJobs();
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
        AndroidJobScheduler.scheduleEvery(getApplicationContext(), AndroidJobSchedulerUtils.persistableBundleToJobInfo(extras));
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobFinished(params, false);
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlutterMain.ensureInitializationComplete(getApplicationContext(), null);
    }
}
