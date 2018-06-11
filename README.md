# android_job_scheduler_example

Schedule Jobs using Android's JobScheduler API. This is very much Work in progress. Much of this Plugin is based on the [android_alarm_manager](https://github.com/flutter/plugins/tree/master/packages/android_alarm_manager/) Plugin.

## Getting Started

### Add the Dependency

Check the latest Version on [pub](https://pub.dartlang.org/packages/android_job_scheduler#-versions-tab-).

Then, in your `pubspec.yml`:

```yaml
dependencies:
  ...
  android_job_scheduler: ^0.0.1
```

### Declare the JobScheduler Service in `AndroidManifest.xml`

In your project's `android/app/src/main` directory, open the `AndroidManifest.xml` and add the following to the `<application />` block:

```xml
<service
    android:exported="true"
    android:permission="android.permission.BIND_JOB_SERVICE"
    android:name="io.gjg.androidjobscheduler.AndroidJobScheduler">
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

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).
