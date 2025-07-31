import 'dart:async';
import 'dart:developer';

import 'package:android_intent_plus/android_intent.dart';
import 'package:android_intent_plus/flag.dart';
import 'package:flutter_accessibility_service/accessibility_event.dart';
import 'package:flutter_accessibility_service/config/overlay_config.dart';
import 'package:flutter_accessibility_service/config/overlay_gravity.dart';
import 'package:flutter_accessibility_service/flutter_accessibility_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AccessibilityManager {
  static final Set<String> _blockedApps = {
    'com.google.android.youtube',
    // 여기에 차단할 앱 패키지명 추가 가능
  };

  /// 접근성 권한이 현재 활성화되어 있는지 확인
  static Future<bool> isPermissionGranted() async {
    return await FlutterAccessibilityService.isAccessibilityPermissionEnabled();
  }

  static StreamSubscription<AccessibilityEvent>? _subscription;
  static DateTime _lastShown = DateTime.fromMillisecondsSinceEpoch(0);
  static bool _initialized = false;

  /// 접근성 초기화
  static Future<void> init() async {
    if (_initialized) return;
    _initialized = true;

    final granted = await isPermissionGranted();
    if (!granted) {
      log('[접근성] 권한이 꺼져 있어 초기화 중단');
      requestPermission();
      return;
    }

    _setupAccessibilityListener();
  }

  /// 접근성 이벤트 스트림 구독 및 오버레이 제어
  static Future<void> _setupAccessibilityListener() async {
    final prefs = await SharedPreferences.getInstance();
    final blockUntilMillis = prefs.getInt('blockReadyUntil') ?? 0;
    final blockUntil = DateTime.fromMillisecondsSinceEpoch(blockUntilMillis);
    final now = DateTime.now();

    _subscription = FlutterAccessibilityService.accessStream.listen((
      event,
    ) async {

      print('=== 접근성 이벤트 감지됨 ===');
      print('패키지: ${event.packageName}');
      print('이벤트타입: ${event.eventType}');
      print('isFocused: ${event.isFocused}');
      print('isActive: ${event.isActive}');
      print('isActionType: ${event.actionType}');

      final pkg = event.packageName;
      if (pkg == null) return;

      if (now.isBefore(blockUntil)) {
        if (_blockedApps.contains(pkg) && event.isFocused == true) {
          if (now.difference(_lastShown).inSeconds > 2) {
            _lastShown = now;
            await FlutterAccessibilityService.showOverlayWindow(
              _buildOverlayConfig(),
            );
          }
        }
      }

      if (event.isFocused == false &&
          DateTime.now().difference(_lastShown).inSeconds > 3) {
        await FlutterAccessibilityService.hideOverlayWindow();
      }
    });
  }

  /// 오버레이 설정 구성
  static OverlayConfig _buildOverlayConfig() {
    return const OverlayConfig().copyWith(
      width: -1,
      height: -1,
      gravity: OverlayGravity.topLeft,
      clickableThrough: false,
    );
  }

  static Future<void> hideOverlayManually() async {
    await FlutterAccessibilityService.hideOverlayWindow();
  }

  static Future<void> requestPermission() async {
    const intent = AndroidIntent(
      action: 'android.settings.ACCESSIBILITY_SETTINGS',
      flags: <int>[Flag.FLAG_ACTIVITY_NEW_TASK],
    );
    await intent.launch();
  }

  static void updateBlockedApps(Set<String> packages) {
    _blockedApps
      ..clear()
      ..addAll(packages);
  }

  static Future<void> dispose() async {
    await _subscription?.cancel();
    _subscription = null;
    _initialized = false;
  }
}
