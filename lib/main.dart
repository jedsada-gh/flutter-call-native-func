import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:developer';

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

  static const _channel =
      MethodChannel('com.example.my_flutter_application/random_number');
  static const method_random_number = 'getRandomNumber';
  static const _eventChannel =
      EventChannel('com.example.my_flutter_application/event');

  late StreamSubscription? _streamSubscription;

  Future<void> _enableEventReceiver() async {
    _streamSubscription =
        _eventChannel.receiveBroadcastStream().listen((event) {
      setState(() {
        _resultEventNumber = "$event";
      });
    }, onError: (error) {
      setState(() {
        _resultEventNumber = 'Received error: ${error.message}';
      });
    }, cancelOnError: true);
  }

  Future<void> _disableEventReceiver() async {
    if (_streamSubscription != null) {
      _streamSubscription?.cancel();
      _streamSubscription = null;
      setState(() {
        _resultNumber = 0;
      });
    }
  }

  Future<void> _getRandomNumber() async {
    try {
      final int result = await _channel.invokeMethod(method_random_number);
      setState(() {
        _resultNumber = result;
      });
    } on PlatformException catch (e) {
      log("error: ${e.message}");
      setState(() {
        _resultNumber = 0;
      });
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
              child: Text('Call native func'),
            ),
            Container(padding: EdgeInsets.all(16), child: Divider()),
            const Text(
              'Result number of event:',
            ),
            Text(
              '$_resultEventNumber',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            Container(
                padding: EdgeInsets.all(16),
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
                      child: Text('Start event with native'),
                    ),
                    TextButton(
                      style: ButtonStyle(
                          foregroundColor: MaterialStateProperty.all<Color>(
                              Colors.deepPurple),
                          side: MaterialStateProperty.all<BorderSide>(
                              const BorderSide(color: Colors.deepPurple))),
                      onPressed: _disableEventReceiver,
                      child: Text('Stop event with native'),
                    )
                  ],
                )),
          ],
        ),
      ),
    );
  }
}