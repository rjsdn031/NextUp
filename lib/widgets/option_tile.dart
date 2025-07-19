import 'package:flutter/material.dart';

class OptionTile extends StatelessWidget {
  final String title;
  final String subtitle;
  final bool value;
  final VoidCallback? onTap;
  final ValueChanged<bool> onSwitch;

  const OptionTile({
    super.key,
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onTap,
    required this.onSwitch,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      title: Text(title, style: const TextStyle(color: Colors.white)),
      subtitle: Text(subtitle, style: const TextStyle(color: Colors.grey)),
      trailing: Switch(
        value: value,
        onChanged: onSwitch,
        activeColor: Colors.grey,
      ),
    );
  }
}
