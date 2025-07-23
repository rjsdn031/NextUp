import 'package:flutter/material.dart';
import '../models/alarm_model.dart';
import '../screens/add_alarm_screen.dart';

class AlarmFAB extends StatelessWidget {
  final void Function(AlarmModel) onAdd;

  const AlarmFAB({super.key, required this.onAdd});

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton(
      backgroundColor: Colors.grey,
      child: const Icon(Icons.add, size: 28),
      onPressed: () async {
        final newAlarm = await Navigator.push<AlarmModel>(
          context,
          MaterialPageRoute(builder: (_) => const AddAlarmScreen()),
        );

        if (newAlarm != null) {
          onAdd(newAlarm);
        }
      },
    );
  }
}
