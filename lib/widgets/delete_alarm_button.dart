import 'package:flutter/material.dart';
import '../utils/dialog_utils.dart';

class DeleteAlarmButton extends StatelessWidget {
  final int index;
  final VoidCallback onDelete;

  const DeleteAlarmButton({
    super.key,
    required this.index,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Align(
        alignment: Alignment.centerLeft,
        child: TextButton.icon(
          onPressed: () async {
            final confirmed = await showConfirmDialog(
              context: context,
              title: '알람 삭제',
              content: '정말 이 알람을 삭제하시겠습니까?',
              confirmText: '삭제',
            );

            if (confirmed == true) {
              onDelete();
            }
          },
          icon: const Icon(Icons.delete, color: Colors.red),
          label: const Text(
            '알람 삭제',
            style: TextStyle(color: Colors.red),
          ),
        ),
      ),
    );
  }
}