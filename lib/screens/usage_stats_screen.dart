import 'package:flutter/material.dart';
import 'package:usage_stats/usage_stats.dart';
import 'package:intl/intl.dart';

class UsageStatsScreen extends StatefulWidget {
  const UsageStatsScreen({super.key});

  @override
  State<UsageStatsScreen> createState() => _UsageStatsScreenState();
}

class _UsageStatsScreenState extends State<UsageStatsScreen> {
  bool _loading = true;
  Map<String, Duration> _usageByApp = {};
  final dateFormat = DateFormat('yyyy-MM-dd HH:mm');

  @override
  void initState() {
    super.initState();
    _loadUsageStats();
  }

  Future<void> _loadUsageStats() async {
    setState(() => _loading = true);

    const String Foreground = '1';
    const String Background = '2';

    final end = DateTime.now();
    final start = end.subtract(const Duration(hours: 1));
    // print('사용 기록 요청 범위: $start ~ $end ===>');

    final permitted = await UsageStats.checkUsagePermission();
    // print('권한 허용 여부: $permitted ===>');

    if (permitted != true) {
      await UsageStats.grantUsagePermission();
      Future.delayed(const Duration(milliseconds: 500), _loadUsageStats);
      // print('권한 요청 후 재시도 예약됨 ===>');
      return;
    }

    List<EventUsageInfo> events = [];
    try {
      events = await UsageStats.queryEvents(start, end);
      // print('이벤트 총 개수: ${events.length} ===>');
      // forground == '1' / background == '2'
      // for (final e in events.where((e) => e.eventType == '1' || e.eventType == '2').take(10)) {
      //   print('이벤트 - type: ${e.eventType}, package: ${e.packageName}, timestamp: ${e.timeStamp} ===>');
      // }
    } catch (e, stack) {
      print('이벤트 수집 실패: $e ===>');
      print('스택트레이스: $stack ===>');
      return;
    }

    final usageMap = <String, Duration>{};
    final lastForegroundTime = <String, DateTime>{};

    print("Events:$events");
    for (final event in events) {
      print('이벤트 처리 중: ${event.packageName}, type: ${event.eventType} ===>');
      final pkg = event.packageName ?? 'unknown';

      final timestampMillis = int.tryParse(event.timeStamp ?? '');
      if (timestampMillis == null) {
        print('파싱 불가 timestamp: ${event.timeStamp} ===>');
        continue;
      }
      final eventTime = DateTime.fromMillisecondsSinceEpoch(timestampMillis);

      if (event.eventType == Foreground) {
        // print('Foreground 기록: $pkg @ $eventTime ===>');
        lastForegroundTime[pkg] = eventTime;
      } else if (event.eventType == Background && lastForegroundTime.containsKey(pkg)) {
        final start = lastForegroundTime[pkg]!;
        final duration = eventTime.difference(start);
        // print('Background 기록: $pkg, 사용 시간: ${duration.inSeconds}초 ===>');
        usageMap[pkg] = (usageMap[pkg] ?? Duration.zero) + duration;
        lastForegroundTime.remove(pkg);
      }
    }

    if (lastForegroundTime.isNotEmpty) {
      print('쌍이 맞지 않아 남은 Foreground 앱: ${lastForegroundTime.keys} ===>');
    }

    final sortedEntries = usageMap.entries.toList()
      ..sort((a, b) => b.value.compareTo(a.value));

    setState(() {
      _usageByApp = Map.fromEntries(sortedEntries);
      _loading = false;
    });

    // print('최종 앱 사용 시간 목록: ===>');
    _usageByApp.forEach((key, value) {
      print('$key → ${value.inMinutes}분 ${value.inSeconds % 60}초 ===>');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('앱별 총 사용 시간')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _usageByApp.isEmpty
          ? const Center(child: Text('앱 사용 기록이 없습니다.'))
          : ListView.builder(
              itemCount: _usageByApp.length,
              itemBuilder: (context, index) {
                final entry = _usageByApp.entries.elementAt(index);
                final minutes = entry.value.inMinutes;
                final seconds = entry.value.inSeconds % 60;
                return ListTile(
                  title: Text(entry.key),
                  subtitle: Text('총 사용 시간: ${minutes}분 ${seconds}초'),
                  onTap: () {
                    // TODO: 상세 화면으로 이동
                  },
                );
              },
            ),
    );
  }
}
