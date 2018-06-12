import 'dart:async';

import 'package:flutter/services.dart';

class AndroidJobInfo {
  int id;
  AndroidJobInfo(this.id);
}

abstract class _JobConstraint {
  const _JobConstraint();
  Map<String, dynamic> serialize();
  String getName();
}

class BackoffCriteria extends _JobConstraint {
  static const BACKOFF_POLICY_LINEAR = 0x00000000;
  static const BACKOFF_POLICY_EXPONENTIAL = 0x00000001;
  static const DEFAULT_INITIAL_BACKOFF_MILLIS = 0x0000000000007530;
  static const MAX_BACKOFF_DELAY_MILLIS = 0x000000000112a880;

  final int initialBackoffMillis;
  final int backoffPolicy;
  const BackoffCriteria({this.initialBackoffMillis, this.backoffPolicy});

  Map<String, dynamic> serialize() {
    return {
      "initialBackoffMillis": this.initialBackoffMillis,
      "backoffPolicy": this.backoffPolicy
    };
  }

  String getName() => "BackoffCriteria";
}

class PersistentAcrossReboots extends _JobConstraint {
  final bool isPersistent;
  const PersistentAcrossReboots({this.isPersistent});

  Map<String, dynamic> serialize() {
    return { "isPersistent": this.isPersistent };
  }

  String getName() => "PersistentAcrossReboots";
}

class RequiredNetworkType extends _JobConstraint {
  static const NETWORK_TYPE_ANY = 1;
  static const NETWORK_TYPE_CELLULAR = 2;
  static const NETWORK_TYPE_METERED = 4;
  static const NETWORK_TYPE_NONE = 0;
  static const NETWORK_TYPE_UNMETERED = 2;
  static const NETWORK_TYPE_NOT_ROAMING = 3;
  final int requiredType;
  const RequiredNetworkType({this.requiredType});

  Map<String, dynamic> serialize() {
    return { "requiredType": this.requiredType };
  }

  String getName() => "RequiredNetworkType";
}

class RequiresDeviceIdle extends _JobConstraint {
  const RequiresDeviceIdle();
  Map<String, dynamic> serialize() {
    return {};
  }
  String getName() => "RequiresDeviceIdle";
}

class RequiresStorageNotLow extends _JobConstraint {
  const RequiresStorageNotLow();
  Map<String, dynamic> serialize() {
    return {};
  }
  String getName() => "RequiresStorageNotLow";
}

class RequiresCharging extends _JobConstraint {
  const RequiresCharging();
  Map<String, dynamic> serialize() {
    return {};
  }
  String getName() => "RequiresCharging";
}

Map<String, Map<String, dynamic>> serializeConstraints(List<_JobConstraint> constraints) {
  if (constraints == null) {
    return null;
  }
  return new Map.fromEntries(
    constraints.map((_JobConstraint constraint) => MapEntry(constraint.getName(), constraint.serialize()))
  );
}


class AndroidJobScheduler {
  static const MethodChannel _channel =
      const MethodChannel('plugins.gjg.io/android_job_scheduler');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> scheduleEvery(Duration every, int id, dynamic Function() function, {int flexMillis = 10000, List<_JobConstraint> constraints}) async {
    _channel.setMethodCallHandler((MethodCall call) {
      switch (call.method) {
        case 'firedWhileApplicationRunning':
          function();
          return;
        default:
          throw 'not implemented';
      }
    });
    final String functionName = _getFunctionName(function);
    if (functionName == null) {
      throw 'scheduleEvery failed: The supplied function can only be a top level function or a static method! Class members'
            ' or Closures are not allowed.';
    }
    return await _channel.invokeMethod('scheduleEvery', [every.inMilliseconds, functionName, id,
        flexMillis, serializeConstraints(constraints)]);
  }

  static Future<void> cancelJob(int id) async {
    return await _channel.invokeMethod('cancelJob', [id]);
  }

  static Future<void> cancelAllJobs() async {
    return await _channel.invokeMethod('cancelAllJobs', []);
  }

  static Future<List<AndroidJobInfo>> getAllPendingJobs() async {
    return (await _channel.invokeMethod('getAllPendingJobs', []) as List<dynamic>)
      .cast<int>().map((int id) => new AndroidJobInfo(id)).toList();
  }
}

String _getFunctionName(dynamic Function() function) {
  final String s = function.toString();
  try {
    return s.substring(s.indexOf('\'') + 1, s.lastIndexOf('\''));
  } on RangeError {
    return null;
  }
}
