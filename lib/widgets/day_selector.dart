import 'package:flutter/material.dart';
import '../models/alarm_model.dart';

class DaySelector extends StatelessWidget {
  final Set<DayOfWeek> selectedDays;
  final Function(Set<DayOfWeek>) onChanged;

  const DaySelector({
    super.key,
    required this.selectedDays,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final dayLabels = ['일', '월', '화', '수', '목', '금', '토'];

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: List.generate(7, (i) {
          final day = DayOfWeek.values[i];
          final label = dayLabels[i];
          final selected = selectedDays.contains(day);

          return GestureDetector(
            onTap: () {
              final updated = {...selectedDays};
              if (selected) {
                updated.remove(day);
              } else {
                updated.add(day);
              }
              onChanged(updated);
            },
            child: CircleAvatar(
              radius: 18,
              backgroundColor: selected ? Colors.white : Colors.grey[700],
              child: Text(label, style: const TextStyle(color: Colors.white)),
            ),
          );
        }),
      ),
    );
  }
}
