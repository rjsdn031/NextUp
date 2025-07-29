import 'package:flutter/material.dart';

class AlarmNameField extends StatelessWidget {
  final String initialValue;
  final ValueChanged<String> onChanged;

  const AlarmNameField({
    super.key,
    this.initialValue = '',
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: TextField(
        style: const TextStyle(color: Colors.white),
        decoration: const InputDecoration(
          labelText: '알람 이름',
          labelStyle: TextStyle(color: Colors.grey),
          enabledBorder: UnderlineInputBorder(
            borderSide: BorderSide(color: Colors.grey),
          ),
        ),
        controller: TextEditingController(text: initialValue),
        onChanged: onChanged,
      ),
    );
  }
}