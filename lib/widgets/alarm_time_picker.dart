import 'package:flutter/material.dart';

class AlarmTimePicker extends StatelessWidget {
  final TimeOfDay time;
  final VoidCallback onTap;

  const AlarmTimePicker({
    super.key,
    required this.time,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 24),
        alignment: Alignment.center,
        child: Text(
          time.format(context),
          style: const TextStyle(fontSize: 48, color: Colors.white),
        ),
      ),
    );
  }
}
