import 'dart:async';

import 'package:flutter/services.dart';

class AndroidJobInfo {
  int id;
  AndroidJobInfo(this.id);
}

class AndroidJobScheduler {
  static const MethodChannel _channel =
      const MethodChannel('plugins.gjg.io/android_job_scheduler');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> scheduleEvery(Duration every, int id, dynamic Function() function) async {
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
    return await _channel.invokeMethod('scheduleEvery', [every.inMilliseconds, functionName, id]);
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
