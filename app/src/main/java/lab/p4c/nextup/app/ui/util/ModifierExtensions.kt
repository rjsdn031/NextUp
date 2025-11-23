package lab.p4c.nextup.app.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable

fun Modifier.clickableThrottle(
    onClick: () -> Unit
): Modifier {
    return this.clickable {
        if (ClickThrottle.allow()) {
            onClick()
        }
    }
}

fun Modifier.clickableThrottle(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    return if (enabled) {
        this.clickable {
            if (ClickThrottle.allow()) onClick()
        }
    } else {
        this
    }
}
