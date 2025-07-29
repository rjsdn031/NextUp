import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../models/usage_session.dart';

class UsageDetailScreen extends StatelessWidget {
  final String appName;
  final List<UsageSession> sessions;

  const UsageDetailScreen({
    super.key,
    required this.appName,
    required this.sessions,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('$appName 사용 기록')),
      body: sessions.isEmpty
          ? const Center(child: Text('사용 기록이 없습니다.'))
          : ListView.builder(
        itemCount: sessions.length,
        itemBuilder: (context, index) {
          final session = sessions[index];
          return ListTile(
            leading: Text('${index + 1}'),
            title: Text(
              '${DateFormat('MM/dd HH:mm').format(session.startTime)} ~ '
                  '${DateFormat('HH:mm').format(session.endTime)}',
            ),
            subtitle: Text(
              '사용 시간: ${session.endTime.difference(session.startTime).inMinutes}분',
            ),
          );
        },
      ),
    );
  }
}