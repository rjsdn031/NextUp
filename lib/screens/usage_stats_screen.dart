import 'package:flutter/material.dart';
import 'package:app_usage/app_usage.dart';
import '../services/usage_stats_service.dart';
import 'package:intl/intl.dart';

class UsageStatsScreen extends StatefulWidget {
  const UsageStatsScreen({super.key});

  @override
  State<UsageStatsScreen> createState() => _UsageStatsScreenState();
}

class _UsageStatsScreenState extends State<UsageStatsScreen> {
  List<AppUsageInfo> _usages = [];
  bool _loading = true;

  final dateFormat = DateFormat('yyyy-MM-dd HH:mm');

  @override
  void initState() {
    super.initState();
    _loadUsageStats();
  }

  Future<void> _loadUsageStats() async {
    setState(() => _loading = true);

    final usageList = await UsageStatsService.fetchUsageStats(
      range: const Duration(days: 7),
    );

    setState(() {
      _usages = usageList.where((e) => e.usage.inSeconds > 0).toList();
        // ..sort((a, b) => b.startDate.compareTo(a.startDate));
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('앱 사용 기록')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _usages.isEmpty
          ? const Center(child: Text('앱 사용 기록이 없습니다.'))
          : ListView.builder(
        itemCount: _usages.length,
        itemBuilder: (context, index) {
          final usage = _usages[index];
          return ListTile(
            title: Text(usage.packageName),
            subtitle: Text(
              '시작: ${dateFormat.format(usage.startDate)}\n'
                  '사용 시간: ${usage.usage.inMinutes}분',
            ),
          );
        },
      ),
    );
  }
}
