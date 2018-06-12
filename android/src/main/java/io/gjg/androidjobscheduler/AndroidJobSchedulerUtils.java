package io.gjg.androidjobscheduler;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//class BackoffCriteria extends _JobConstraint {
//    static const BACKOFF_POLICY_LINEAR = 0x00000000;
//    static const BACKOFF_POLICY_EXPONENTIAL = 0x00000001;
//    static const DEFAULT_INITIAL_BACKOFF_MILLIS = 0x0000000000007530;
//    static const MAX_BACKOFF_DELAY_MILLIS = 0x000000000112a880;
//
//    int initialBackoffMillis;
//    int backoffPolicy;
//    BackoffCriteria({this.initialBackoffMillis, this.backoffPolicy});
//
//    Map<String, dynamic> serialize() {
//        return {
//                "initialBackoffMillis": this.initialBackoffMillis,
//                "backoffPolicy": this.backoffPolicy
//    };
//    }
//
//    String getName() => "BackoffCriteria";
//}
//
//class PersistentAcrossReboots extends _JobConstraint {
//    bool isPersistent;
//    PersistentAcrossReboots({this.isPersistent});
//
//    Map<String, dynamic> serialize() {
//        return { "isPersistent": this.isPersistent };
//    }
//
//    String getName() => "PersistentAcrossReboots";
//}
//
//class RequiredNetworkType extends _JobConstraint {
//    static const NETWORK_TYPE_ANY = 1;
//    static const NETWORK_TYPE_CELLULAR = 2;
//    static const NETWORK_TYPE_METERED = 4;
//    static const NETWORK_TYPE_NONE = 0;
//    static const NETWORK_TYPE_UNMETERED = 2;
//    static const NETWORK_TYPE_NOT_ROAMING = 3;
//    int requiredType;
//    RequiredNetworkType({this.requiredType});
//
//    Map<String, dynamic> serialize() {
//        return { "requiredType": this.requiredType };
//    }
//
//    String getName() => "RequiredNetworkType";
//}
//
//class RequiresDeviceIdle extends _JobConstraint {
//    Map<String, dynamic> serialize() {
//        return {};
//    }
//    String getName() => "RequiresDeviceIdle";
//}
//
//class RequiresStorageNotLow extends _JobConstraint {
//    Map<String, dynamic> serialize() {
//        return {};
//    }
//    String getName() => "RequiresStorageNotLow";
//}
//
//class RequiresCharging extends _JobConstraint {
//    Map<String, dynamic> serialize() {
//        return {};
//    }
//    String getName() => "RequiresCharging";
//}

@TargetApi(Build.VERSION_CODES.M)
public class AndroidJobSchedulerUtils {
    private static final String TAG = AndroidJobSchedulerUtils.class.getSimpleName();
    public static final String B_KEY_FLEX_MILLIS = "flexMillis";
    public static final String B_KEY_RESCHEDULE = "reschedule";
    public static final String B_KEY_INTERVAL = "interval";
    public static final String B_KEY_DART_CB = "callback";
    public static final String B_KEY_ID = "id";
    public static final String B_KEY_COMPONENT_NAME = "componentName";
    public static final String B_KEY_COMPONENT_PKG = "componentPkg";
    public static final String B_KEY_DEVICE_IDLE = "RequiresDeviceIdle";

    public static final String B_KEY_NETWORK_TYPE = "RequiresNetworkType";
    public static final String B_INNER_REQUIRED_NETWORK = "requiredType";

    public static final String B_KEY_REQUIRES_CHARGING = "RequiresCharging";
    public static final String B_KEY_PERSISTENT_ACROSS_REBOOTS = "PersistentAcrossReboots";

    public static final String B_KEY_BACKOFF_CRITERIA = "BackoffCriteria";
    public static final String B_INNER_BACKOFF_MILLIS = "initialBackoffMillis";
    public static final String B_INNER_BACKOFF_POLICY = "backoffPolicy";

    public static PersistableBundle serializedDataToPersistableBundle(List<?> args, Context context) {
        final ComponentName name = new ComponentName(context, AndroidJobScheduler.class);
        final Integer every = (Integer) args.get(0);
        final String funcCallback = (String) args.get(1);
        final Integer id = (Integer) args.get(2);
        final Integer flexMillis = (Integer) args.get(3);
        final Map<String, Map<String, Object>> constraints = (Map<String, Map<String, Object>>) args.get(4);
        PersistableBundle job = new PersistableBundle();
        job.putString(B_KEY_RESCHEDULE, funcCallback);
        job.putInt(B_KEY_ID, id);
        job.putInt(B_KEY_INTERVAL, every);
        job.putInt(B_KEY_FLEX_MILLIS, flexMillis);
        job.putString(B_KEY_DART_CB, funcCallback);
        job.putString(B_KEY_COMPONENT_PKG, name.getPackageName());
        job.putString(B_KEY_COMPONENT_NAME, name.getClassName());
        if (constraints != null) {
            for (Map.Entry<String, Map<String, Object>> entry : constraints.entrySet()) {
                PersistableBundle innerBundle = new PersistableBundle();
                for (Map.Entry<String, Object> innerEntry : entry.getValue().entrySet()) {
                    innerBundle.putInt(innerEntry.getKey(), (Integer) innerEntry.getValue());
                }
                job.putPersistableBundle(entry.getKey(), innerBundle);
            }
        }
        return job;
    }

    public static JobInfo persistableBundleToJobInfo(PersistableBundle bundle) {
        JobInfo.Builder builder = new JobInfo.Builder(bundle.getInt(B_KEY_ID),
                new ComponentName(bundle.getString(B_KEY_COMPONENT_PKG), bundle.getString(B_KEY_COMPONENT_NAME)))
                    .setMinimumLatency(bundle.getInt(B_KEY_INTERVAL))
                    .setExtras(bundle);

        if (bundle.containsKey(B_KEY_PERSISTENT_ACROSS_REBOOTS)) {
            builder.setPersisted(true);
        }
        if (bundle.containsKey(B_KEY_REQUIRES_CHARGING)) {
            builder.setRequiresCharging(true);
        }
        if (bundle.containsKey(B_KEY_BACKOFF_CRITERIA)) {
            builder.setBackoffCriteria(
                    bundle.getPersistableBundle(B_KEY_BACKOFF_CRITERIA).getInt(B_INNER_BACKOFF_MILLIS),
                    bundle.getPersistableBundle(B_KEY_BACKOFF_CRITERIA).getInt(B_INNER_BACKOFF_POLICY)
            );
        }
        if (bundle.containsKey(B_KEY_NETWORK_TYPE)) {
            builder.setRequiredNetworkType(
                    bundle.getPersistableBundle(B_KEY_NETWORK_TYPE).getInt(B_INNER_REQUIRED_NETWORK)
            );
        }

        return builder.build();
    }
}
