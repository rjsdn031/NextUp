import 'package:usage_stats/usage_stats.dart';
import 'package:android_intent_plus/android_intent.dart';
import 'package:android_intent_plus/flag.dart';

class UsageStatsService {
  static Future<Map<String, Duration>> fetchUsageStats({
    Duration range = const Duration(hours: 1),
  }) async {
    final end = DateTime.now();
    final start = end.subtract(range);

    final isPermitted = await UsageStats.checkUsagePermission();
    if (isPermitted != true) {
      await requestPermission();
      return {};
    }

    List<EventUsageInfo> events = [];

    try {
      events = await UsageStats.queryEvents(start, end);
      print('이벤트 개수: ${events.length}');
    } catch (e, stack) {
      print('이벤트 수집 실패: $e');
    }

    for (final event in events.take(50)) {
      print('[UsageStats] 이벤트: '
          'package=${event.packageName}, '
          'type=${event.eventType}, '
          'timestamp=${event.timeStamp}');
    }

    final usageMap = <String, Duration>{};
    final lastForeground = <String, DateTime>{};

    for (final event in events) {
      final pkg = event.packageName ?? 'unknown';

      if (event.eventType == '1') {
        lastForeground[pkg] = DateTime.parse(event.timeStamp!);
      } else if (event.eventType == '2' && lastForeground.containsKey(pkg)) {
        final startTime = lastForeground[pkg]!;
        final endTime = DateTime.parse(event.timeStamp!);
        final duration = endTime.difference(startTime);
        usageMap[pkg] = (usageMap[pkg] ?? Duration.zero) + duration;
        lastForeground.remove(pkg);
      }
    }

    return usageMap;
  }

  static Future<void> requestPermission() async {
    const intent = AndroidIntent(
      action: 'android.settings.USAGE_ACCESS_SETTINGS',
      flags: <int>[Flag.FLAG_ACTIVITY_NEW_TASK],
    );
    await intent.launch();
  }
}
