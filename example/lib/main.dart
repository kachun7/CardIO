import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:card_io/card_io.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String? cardNumber;

  @override
  Widget build(BuildContext context) => MaterialApp(
        home: Scaffold(
          appBar: AppBar(
            title: const Text('Card IO Example App'),
          ),
          body: Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Card number:'),
                if (cardNumber != null) Text(cardNumber!),
              ],
            ),
          ),
          floatingActionButton: FloatingActionButton(
            onPressed: _scan,
            child: Icon(Icons.camera_alt_outlined),
          ),
        ),
      );

  Future<void> _scan() async {
    try {
      final cardNumber = await CardIo.scanCard();

      if (cardNumber != null) {
        setState(() {
          this.cardNumber = cardNumber;
        });
      }
      print('CardInformation: $cardNumber');
    } on PlatformException catch (error) {
      print('CardInformation-error: $error');
    }
  }
}
