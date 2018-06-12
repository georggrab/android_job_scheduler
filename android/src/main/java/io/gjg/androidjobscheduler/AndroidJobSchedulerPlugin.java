package io.gjg.androidjobscheduler;

import android.app.job.JobInfo;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.flutter.app.FlutterPluginRegistry;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            result.error("JobScheduler API is not available in API Level 20 and below.", "", null);
            return;
        }
        if (call.method.equals("scheduleEvery")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PersistableBundle bundle = AndroidJobSchedulerUtils.serializedDataToPersistableBundle((ArrayList<?>) call.arguments, mContext);

                AndroidJobScheduler.scheduleEvery(this.mContext, AndroidJobSchedulerUtils.persistableBundleToJobInfo(bundle));
                result.success(true);
            } else {
                result.success(false);
            }
        } else if (call.method.equals("cancelJob")) {
            final ArrayList<?> args = (ArrayList<?>) call.arguments;
            AndroidJobScheduler.cancelJob(this.mContext, (Integer) args.get(0));
        } else if (call.method.equals("cancelAllJobs")) {
            AndroidJobScheduler.cancelAllJobs(this.mContext);
        } else if (call.method.equals("getAllPendingJobs")) {
            List<JobInfo> jobs = AndroidJobScheduler.getAllPendingJobs(this.mContext);
            List<Integer> jobIds = new ArrayList<>();
            for(JobInfo job : jobs) {
                jobIds.add(job.getId());
            }
            result.success(jobIds);
        } else {
            result.notImplemented();
        }
    }
}
