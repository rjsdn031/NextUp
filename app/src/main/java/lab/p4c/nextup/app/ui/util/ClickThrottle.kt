package lab.p4c.nextup.app.ui.util

object ClickThrottle {
    private const val Cooldown = 150L
    private var last = 0L

    fun allow(): Boolean {
        val now = System.currentTimeMillis()
        return if (now - last > Cooldown) {
            last = now
            true
        } else false
    }
}
