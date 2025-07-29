import 'package:flutter/material.dart';

class BottomActionButtons extends StatelessWidget {
  final VoidCallback onCancel;
  final VoidCallback onSave;

  const BottomActionButtons({required this.onCancel, required this.onSave, super.key});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: TextButton(
            onPressed: onCancel,
            child: const Text('취소', style: TextStyle(fontSize: 18, color: Colors.white70)),
          ),
        ),
        Expanded(
          child: TextButton(
            onPressed: onSave,
            child: const Text('저장', style: TextStyle(fontSize: 18, color: Colors.white)),
          ),
        ),
      ],
    );
  }
}