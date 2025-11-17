package lab.p4c.nextup.feature.settings.ui.model

import android.graphics.drawable.Drawable

data class BlockTargetItemUi(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val checked: Boolean
)