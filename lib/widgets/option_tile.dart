import 'package:flutter/material.dart';

class OptionTile extends StatelessWidget {
  final String title;
  final String? subtitle;
  final bool value;
  final VoidCallback? onTap;
  final ValueChanged<bool>? onSwitch;

  const OptionTile({
    super.key,
    required this.title,
    this.subtitle,
    required this.value,
    this.onTap,
    this.onSwitch,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      title: Text(title, style: const TextStyle(color: Colors.white)),
      subtitle: subtitle != null
          ? Text(subtitle!, style: const TextStyle(color: Colors.grey))
          : null,
      trailing: onSwitch != null
          ? Switch(
        value: value,
        onChanged: onSwitch,
        activeColor: Colors.grey,
      )
          : null,
    );
  }
}
