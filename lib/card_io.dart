import 'dart:async';

import 'package:flutter/services.dart';

class CardIo {
  static const MethodChannel _channel = const MethodChannel('card_io');

  static Future<String?> scanCard({String? scanInstructions}) {
    var args = <String, dynamic>{};
    if (scanInstructions != null) {
      args.putIfAbsent('scanInstructions', () => scanInstructions);
    }
    return _channel.invokeMethod('scanCard', args).then((value) {
      if (value == null) return null;
      final json = Map<String, dynamic>.from(value);
      return json['cardNumber'];
    });
  }
}
