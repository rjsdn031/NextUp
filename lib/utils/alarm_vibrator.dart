import 'dart:async';
import 'package:vibration/vibration.dart';

class AlarmVibrator {
  Timer? _vibrationTimer;

  /// start vibrate
  Future<void> start() async {
    final canVibrate = await Vibration.hasVibrator() ?? false;
    if (!canVibrate) return;

    _vibrationTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      Vibration.vibrate(pattern: [0, 1000]); // 0ms 대기 후 1000ms 진동
    });
  }

  /// stop vibrate
  void stop() {
    _vibrationTimer?.cancel();
    _vibrationTimer = null;
    Vibration.cancel();
  }

  /// isActive?
  bool get isActive => _vibrationTimer?.isActive ?? false;
}