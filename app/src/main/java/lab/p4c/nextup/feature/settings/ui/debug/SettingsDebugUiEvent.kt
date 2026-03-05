package lab.p4c.nextup.feature.settings.ui.debug

sealed interface SettingsDebugUiEvent {
    data class Toast(val message: String) : SettingsDebugUiEvent
}