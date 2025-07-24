import 'package:app_usage/app_usage.dart';
import 'package:android_intent_plus/android_intent.dart';
import 'package:android_intent_plus/flag.dart';

class UsageStatsService {
  static Future<List<AppUsageInfo>> fetchUsageStats({
    Duration range = const Duration(days: 7),
  }) async {
    final end = DateTime.now();
    final start = end.subtract(range);

    try {
      final appUsage = AppUsage();
      return await appUsage.getAppUsage(start, end);
    } catch (e, stack) {
      if (e is Exception && e.toString().contains("permission_not_granted")) {
        await requestPermission();
        return [];
      }
      print("UsageStatsService error: $e\n$stack");
      rethrow;
    }
  }

  static Future<void> requestPermission() async {
    final now = DateTime.now();
    final dummy = AppUsage();

    try {
      await dummy.getAppUsage(now.subtract(const Duration(minutes: 1)), now);
    } catch (_) {}

    const intent = AndroidIntent(
      action: 'android.settings.USAGE_ACCESS_SETTINGS',
      flags: <int>[Flag.FLAG_ACTIVITY_NEW_TASK],
    );
    await intent.launch();
  }
}