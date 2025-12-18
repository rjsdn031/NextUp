package lab.p4c.nextup.feature.overlay.infra

import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.overlay.port.OverlayVisibility
import lab.p4c.nextup.feature.overlay.ui.OverlayState

@Singleton
class OverlayVisibilityImpl @Inject constructor() : OverlayVisibility {
    override fun isOverlayVisible(): Boolean = OverlayState.isRunning
}
