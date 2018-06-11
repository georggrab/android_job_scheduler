import 'dart:io';
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'package:android_job_scheduler/android_job_scheduler.dart';

import 'package:path_provider/path_provider.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

void yolo() async {
  // Search Logcat for Yolo to see the the Callback firing
  // when the app is not running.
  print('Yolo executing');
  final file = await getCommonStateFile();
  if (!await file.exists()) {
    await file.writeAsString("0");
  } else {
    final contents = await file.readAsString();
    print("Contents: $contents");
    final timesCalled = int.parse(contents);
    await file.writeAsString("${timesCalled + 1}");
  }
}

Future<File> getCommonStateFile() async {
  final targetDir = await getApplicationDocumentsDirectory();
  return new File("${targetDir.path}/times_called.txt");
}

class _MyAppState extends State<MyApp> {
  bool _jobIsInstalled;
  int _timesCalled = 0;

  @override
  initState() {
    super.initState();
    initFileWatcher();
    updateCallBackTimesCalled();
  }

  initFileWatcher() async {
    Timer.periodic(const Duration(seconds: 5), updateCallBackTimesCalled);
  }

  updateCallBackTimesCalled([Timer _]) async {
    final file = await getCommonStateFile();
    var timesCalled;
    if (!await file.exists()) {
      timesCalled = 0;
    } else {
      timesCalled = int.parse(await file.readAsString());
    }
    setState(() {
      _timesCalled = timesCalled;
    });
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
          appBar: new AppBar(
            title: new Text('Android Job Scheduler'),
          ),
          body: new Center(
              child: new Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              new Text(_jobIsInstalled == null? 'Job installation failed!' : _jobIsInstalled? 'Job is installed!' : 'Job is not installed!'),
              new Text('Callback has been called '),
              new Text('$_timesCalled', textScaleFactor: 2.0),
              new Text('times.'),
              new Divider(),
              new Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    new RaisedButton.icon(
                        onPressed: () async {
                          bool jobIsInstalled;
                          try {
                            jobIsInstalled =
                                await AndroidJobScheduler.scheduleEvery(
                                    const Duration(seconds: 10), 42, yolo);
                          } finally {
                            setState(() {
                              _jobIsInstalled = jobIsInstalled == true;
                            });
                          }
                        },
                        icon: const Icon(Icons.check_box),
                        label: Text('Install Job')),
                    new Container(width: 10.0,),
                    new RaisedButton.icon(
                        onPressed: () {
                          AndroidJobScheduler.cancelJob(42);
                          setState(() {
                            _jobIsInstalled = false;
                          });
                        },
                        icon: const Icon(Icons.delete),
                        label: Text('Uninstall Job')),
                  ]),
              new Divider(),
              new RaisedButton.icon(onPressed: () async {
                final file = await getCommonStateFile();
                if (await file.exists()) {
                  await file.delete();
                  await updateCallBackTimesCalled();
                }
              }, icon: const Icon(Icons.fast_rewind), label: const Text('Reset State')),
              new Container(
                padding: EdgeInsets.all(20.0),
                child: const Text('Close the App, wait a few secs, and see what happens!'),
              )
              
            ],
          ))),
    );
  }
}
