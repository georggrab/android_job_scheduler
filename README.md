# android_job_scheduler_example

Schedule Jobs using Android's JobScheduler API. This is very much Work in progress. Much of this Plugin is based on the [android_alarm_manager](https://github.com/flutter/plugins/tree/master/packages/android_alarm_manager/) Plugin.

## Getting Started

### Add the Dependency

Check the latest Version on [pub](https://pub.dartlang.org/packages/android_job_scheduler#-versions-tab-).

Then, in your `pubspec.yml`:

```yaml
dependencies:
  ...
  android_job_scheduler: ^0.0.4
```

### Declare the JobScheduler Service in `AndroidManifest.xml`

In your project's `android/app/src/main` directory, open the `AndroidManifest.xml` and add the following to the `<application />` block:

```xml
<service
    android:exported="false"
    android:permission="android.permission.BIND_JOB_SERVICE"
    android:name="io.gjg.androidjobscheduler.AndroidJobScheduler" />
```

### Usage

```dart
import 'package:android_job_scheduler/android_job_scheduler.dart';
```

#### Installing Jobs

```dart
// This MUST be a top level Function or a Static Class Member. It may not be a Class Method
// or a Closure. Inside this method, you don't have access to anything assuming a running
// Application.
void iRunPeriodically() {
    print('This gets run periodically by the Android JobScheduler API. '
          'Even when the App is not running. '
          'The Timings are not guaranteed to be exact by the Android OS, though.');
}

void main() {
    ...
    bool jobIsInstalled =
        // For every distinct Job you wish to create, set a new JobId.
        // Calling this Function twice with the same JobId will have no effect.
        await AndroidJobScheduler.scheduleEvery(
            const Duration(seconds: 10), iRunPeriodically, 42);
}
```

#### Canceling Jobs

```dart
void main() {
  ...
  // Cancel a unique Job
  await AndroidJobScheduler.cancelJob(42);
  // Cancel all Jobs scheduled by this Application
  await AndroidJobScheduler.cancelAllJobs();
}
```

#### Get Pending Jobs
```dart
void main() {
    ...
    await AndroidJobScheduler.scheduleEvery(
        const Duration(seconds: 10), iRunPeriodically, 42);
    await AndroidJobScheduler.scheduleEvery(
        const Duration(seconds: 10), iRunPeriodically, 43);
    
    final List<AndroidJobInfo> pendingJobs = await AndroidJobScheduler.getAllPendingJobs();
    print(pendingJobs.map((AndroidJobInfo i) => i.id).toList().join(", ")); // 42, 43
}
```

#### Using other Plugins from inside your callback

In order to use other Plugins from inside the Dart Callback, you'll need to enable the Scheduler to register with your Applications Main Plugin Registry. Do this by providing a custom Application Implementation in your Android Code:

**Warning!** Plugins generally have access to the `FlutterActivity`, in case they need access to any resources they provided by it. However, since we're running in a Job Context and not inside an Activity, the Activity will be `null`. Some Plugins might not expect this and crash on initialization. Plugins that fundamentally need Activities to function will not work when the main Application is not running. Your callbacks should still work if your Application is running, though.

```java
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
```

Don't forget to reference your custom Application Implementation in `AndroidManifest.xml`.

```xml
...
<application
    android:name=".MainApplication">
    ...
</application>
```

## FAQ

### My Callbacks won't work with Plugins

One of the Plugins you're using is expecting to be run inside an Activity. Because there is no Activity when running in a Job Context, this will not work (see above).

### I'm getting an IllegalArgumentException when trying to Schedule anything

```bash
E/MethodChannel#plugins.gjg.io/android_job_scheduler(31525): Failed to handle method call
E/MethodChannel#plugins.gjg.io/android_job_scheduler(31525): java.lang.IllegalArgumentException: No such service ComponentInfo{com.example.dailelog/io.gjg.androidjobscheduler.AndroidJobScheduler}
```

You forgot adding the JobScheduler Service to your `AndroidManifest.xml`. Please do this by following the steps outlined above.

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

### What is the difference to the android_alarm_manager plugin?
Most importantly it uses JobScheduler API instead of the AlarmManager API.

The main reason this plugin exists is that at the moment, android_alarm_manager is not working with the latest Flutter SDK Version. See [this issue](https://github.com/flutter/flutter/issues/17566). If there is Interest from the Flutter Devs, I'll merge the changes relevant for fixing this issue back into android_alarm_manager. 

Apart from that, the JobScheduler API offers a lot more Flexibility than the AlarmManager API for scheduling Jobs. For example, we can tell this API to only schedule Jobs when the Device is Charging, or when a unmetered Network Connection is available. However, none of these features are exposed here atm, though they could be pursued in the Future.
