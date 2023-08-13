import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _resultNumber = 0;
  String _resultEventNumber = "0";

  static const _bridgeChannel =
      MethodChannel("com.example.my_flutter_application/bridge_native");
  // ignore: constant_identifier_names
  static const RANDOM_NUMBER_METHOD = 'getRandomNumber';
  static const NAVIGATE_TO_FACE_DETECTION_PAGE_METHOD =
      "navigateToFaceDetectionPage";
  static const _eventChannel =
      EventChannel('com.example.my_flutter_application/event');

  // ignore: avoid_init_to_null
  late StreamSubscription? _streamSubscription = null;

  _MyHomePageState() {
    _bridgeChannel.setMethodCallHandler(methodHandler);
  }

  Future<void> methodHandler(MethodCall call) async {
    switch (call.method) {
      case "enableEventReceiver":
        _enableEventReceiver();
        break;
      case "disableEventReceiver":
        _disableEventReceiver();
        break;
      default:
        print('no method handler for method ${call.method}');
    }
  }

  void _showToast(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        duration: const Duration(milliseconds: 300),
      ),
    );
  }

  Future<void> _enableEventReceiver() async {
    _streamSubscription =
        _eventChannel.receiveBroadcastStream().listen((event) {
      Map<String, dynamic> data = jsonDecode(event);

      String eventName = data["name"];
      dynamic eventData = data["data"];

      if (eventName == "randomNumber") {
        setState(() {
          _resultEventNumber = "$eventData";
        });
      } else if (eventName == "faceDetection") {
        print("Result: $eventData");
      }
    }, onError: (error) {
      print('Received error: ${error.message}');
      setState(() {
        _resultEventNumber = 'Received error: ${error.message}';
      });
    }, cancelOnError: true);
  }

  Future<void> _disableEventReceiver() async {
    if (_streamSubscription != null) {
      _showToast("The listener event has been successfully stopped");
      _streamSubscription?.cancel();
      _streamSubscription = null;
    } else {
      _showToast("Not found any events to listener");
    }
  }

  Future<void> _getRandomNumber() async {
    try {
      final int result =
          await _bridgeChannel.invokeMethod(RANDOM_NUMBER_METHOD);
      setState(() {
        _resultNumber = result;
      });
    } on PlatformException catch (e) {
      print("error: ${e.message}");
      setState(() {
        _resultNumber = 0;
      });
    }
  }

  Future<void> _navigateToFaceDetectionPage() async {
    try {
      await _bridgeChannel.invokeMethod(NAVIGATE_TO_FACE_DETECTION_PAGE_METHOD);
    } on PlatformException catch (e) {
      print("error: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Result of random number:',
            ),
            Text(
              '$_resultNumber',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            TextButton(
              style: ButtonStyle(
                foregroundColor: MaterialStateProperty.all<Color>(Colors.white),
                backgroundColor:
                    MaterialStateProperty.all<Color>(Colors.deepPurple),
              ),
              onPressed: _getRandomNumber,
              child: const Text('Call native func'),
            ),
            Container(
                padding: const EdgeInsets.all(16), child: const Divider()),
            const Text(
              'Result number of event:',
            ),
            Text(
              _resultEventNumber,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            Container(
                padding: const EdgeInsets.all(16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(
                      style: ButtonStyle(
                        foregroundColor:
                            MaterialStateProperty.all<Color>(Colors.white),
                        backgroundColor:
                            MaterialStateProperty.all<Color>(Colors.deepPurple),
                      ),
                      onPressed: _enableEventReceiver,
                      child: const Text('Start listener'),
                    ),
                    TextButton(
                      style: ButtonStyle(
                          foregroundColor: MaterialStateProperty.all<Color>(
                              Colors.deepPurple),
                          side: MaterialStateProperty.all<BorderSide>(
                              const BorderSide(color: Colors.deepPurple))),
                      onPressed: _disableEventReceiver,
                      child: const Text('Stop listener'),
                    )
                  ],
                )),
            Container(
                padding: const EdgeInsets.all(16), child: const Divider()),
            TextButton(
              style: ButtonStyle(
                foregroundColor: MaterialStateProperty.all<Color>(Colors.white),
                backgroundColor:
                    MaterialStateProperty.all<Color>(Colors.deepPurple),
              ),
              onPressed: _navigateToFaceDetectionPage,
              child: const Text('Face Detection'),
            ),
          ],
        ),
      ),
    );
  }
}
