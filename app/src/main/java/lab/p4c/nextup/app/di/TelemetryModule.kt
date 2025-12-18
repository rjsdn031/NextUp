package lab.p4c.nextup.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.domain.overlay.port.OverlayVisibility
import lab.p4c.nextup.core.domain.telemetry.port.AlarmLoggingWindow
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.port.DateKeyProvider
import lab.p4c.nextup.core.domain.telemetry.port.EventIdGenerator
import lab.p4c.nextup.core.domain.telemetry.port.TelemetrySink
import lab.p4c.nextup.core.domain.telemetry.port.UserIdProvider
import lab.p4c.nextup.feature.overlay.infra.OverlayVisibilityImpl
import lab.p4c.nextup.platform.telemetry.id.UuidEventIdGenerator
import lab.p4c.nextup.platform.telemetry.sink.JsonlTelemetrySink
import lab.p4c.nextup.platform.telemetry.time.SystemDateKeyProvider
import lab.p4c.nextup.platform.telemetry.user.LocalUserIdProvider
import lab.p4c.nextup.platform.telemetry.window.PrefsAlarmLoggingWindow

@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryModule {

    @Binds
    @Singleton
    abstract fun bindTelemetrySink(impl: JsonlTelemetrySink): TelemetrySink

    @Binds
    @Singleton
    abstract fun bindEventIdGenerator(impl: UuidEventIdGenerator): EventIdGenerator

    @Binds
    @Singleton
    abstract fun bindUserIdProvider(impl: LocalUserIdProvider): UserIdProvider

    @Binds
    @Singleton
    abstract fun bindDateKeyProvider(impl: SystemDateKeyProvider): DateKeyProvider

    @Binds
    @Singleton
    abstract fun bindBlockingUiState(impl: OverlayVisibilityImpl): OverlayVisibility

    @Binds
    @Singleton
    abstract fun bindAlarmLoggingWindow(impl: PrefsAlarmLoggingWindow): AlarmLoggingWindow

}
