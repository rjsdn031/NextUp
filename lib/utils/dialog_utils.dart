import 'package:flutter/material.dart';

Future<String?> showOptionDialog(
  BuildContext context,
  String title,
  List<String> options,
  String selectedOption,
) async {
  return await showDialog<String>(
    context: context,
    builder: (context) {
      return SimpleDialog(
        title: Text(title),
        children: options.map((option) {
          return RadioListTile<String>(
            title: Text(option),
            value: option,
            groupValue: selectedOption,
            onChanged: (val) => Navigator.pop(context, val),
          );
        }).toList(),
      );
    },
  );
}

Future<double?> showSliderDialog({
  required BuildContext context,
  required String title,
  required double initial,
  required double min,
  required double max,
  int? divisions,
  required String Function(double) formatValue,
}) async {
  double current = initial;
  return showDialog<double>(
    context: context,
    builder: (context) {
      return AlertDialog(
        title: Text(title),
        content: StatefulBuilder(
          builder: (context, setState) {
            return Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Slider(
                  value: current,
                  onChanged: (val) => setState(() => current = val),
                  min: min,
                  max: max,
                  divisions: divisions,
                  label: formatValue(current),
                ),
                Text(formatValue(current)),
              ],
            );
          },
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('취소'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, current),
            child: const Text('확인'),
          ),
        ],
      );
    },
  );
}

Future<bool?> showConfirmDialog({
  required BuildContext context,
  required String title,
  required String content,
  String cancelText = '취소',
  String confirmText = '확인',
  Color confirmColor = Colors.red,
}) {
  return showDialog<bool>(
    context: context,
    builder: (ctx) => AlertDialog(
      title: Text(title),
      content: Text(content),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(ctx, false),
          child: Text(cancelText),
        ),
        TextButton(
          onPressed: () => Navigator.pop(ctx, true),
          child: Text(confirmText, style: TextStyle(color: confirmColor)),
        ),
      ],
    ),
  );
}
