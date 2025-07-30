import 'package:flutter/material.dart';

class SwipeToDismissButton extends StatefulWidget {
  final VoidCallback onDismiss;

  const SwipeToDismissButton({super.key, required this.onDismiss});

  @override
  State<SwipeToDismissButton> createState() => _SwipeToDismissButtonState();
}

class _SwipeToDismissButtonState extends State<SwipeToDismissButton>
    with SingleTickerProviderStateMixin {
  double dragX = 0.0;
  static const double maxDragDistance = 200.0;
  static const double dismissThreshold = 0.7; // 70% 이상 밀면 dismiss

  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: const Duration(milliseconds: 300));
  }

  void _animateBack() {
    _animation = Tween<double>(begin: dragX, end: 0).animate(_controller)
      ..addListener(() {
        setState(() {
          dragX = _animation.value;
        });
      });
    _controller.forward(from: 0);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _onPanUpdate(DragUpdateDetails details) {
    setState(() {
      dragX += details.delta.dx;
      if (dragX < 0) dragX = 0; // 왼쪽으로는 못 움직이게
      if (dragX > maxDragDistance) dragX = maxDragDistance; // 오른쪽 한계
    });
  }

  void _onPanEnd(DragEndDetails details) {
    if (dragX >= maxDragDistance * dismissThreshold) {
      widget.onDismiss();
    } else {
      _animateBack();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: maxDragDistance + 60,
      height: 80,
      decoration: BoxDecoration(
        color: Colors.white12,
        borderRadius: BorderRadius.circular(40),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: Stack(
        alignment: Alignment.centerLeft,
        children: [
          const Center(
            child: Text(
              'SWIPE',
              style: TextStyle(color: Colors.white70, fontSize: 16),
            ),
          ),
          Positioned(
            left: dragX,
            child: GestureDetector(
              onPanUpdate: _onPanUpdate,
              onPanEnd: _onPanEnd,
              child: Container(
                width: 64,
                height: 64,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white,
                ),
                child: const Icon(Icons.lock_open, color: Colors.black),
              ),
            ),
          ),
        ],
      ),
    );
  }
}