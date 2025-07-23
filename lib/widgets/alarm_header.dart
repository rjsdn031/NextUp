import 'package:flutter/material.dart';
import '../utils/alarm_utils.dart';

class AlarmHeader extends StatelessWidget {
  final DateTime now;
  final String nextAlarmMessage;

  const AlarmHeader({
    super.key,
    required this.now,
    required this.nextAlarmMessage,
  });

  @override
  Widget build(BuildContext context) {
    final nowFormatted = formatDateTime(now);

    return SizedBox(
      height: MediaQuery.of(context).size.height * 0.3,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 32),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            const Spacer(),
            Text(
              nextAlarmMessage,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              nowFormatted,
              style: const TextStyle(
                color: Colors.grey,
                fontSize: 16,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
