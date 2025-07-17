import 'package:flutter/material.dart';

class AlarmTile extends StatelessWidget {
  final String time;
  final List<String> days;
  final bool enabled;
  final ValueChanged<bool>? onToggle;

  const AlarmTile({
    super.key,
    required this.time,
    required this.days,
    required this.enabled,
    this.onToggle,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      decoration: BoxDecoration(
        color: Colors.grey[900],
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          // 시간 + 요일 정보
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  time,
                  style: TextStyle(
                    color: enabled ? Colors.white : Colors.grey,
                    fontSize: 28,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  days.join(' '),
                  style: TextStyle(
                    color: enabled ? Colors.white70 : Colors.grey,
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),

          // 스위치
          Switch(
            value: enabled,
            onChanged: onToggle,
            activeColor: Colors.grey,
          ),
        ],
      ),
    );
  }
}