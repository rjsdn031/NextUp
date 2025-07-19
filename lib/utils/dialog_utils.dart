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
